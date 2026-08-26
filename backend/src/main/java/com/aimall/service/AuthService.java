package com.aimall.service;

import cn.hutool.crypto.digest.BCrypt;
import com.aimall.common.BusinessException;
import com.aimall.common.JwtUtil;
import com.aimall.common.UserContext;
import com.aimall.entity.User;
import com.aimall.mapper.UserMapper;
import com.aimall.mapper.WalletMapper;
import com.aimall.entity.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final WalletMapper walletMapper;
    private final JwtUtil jwtUtil;

    // P1-5 修复：登录失败计数器，防止暴力破解
    private final ConcurrentHashMap<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5; // 最多5次失败
    private static final long LOCKOUT_DURATION = 3600000; // 锁定1小时

    public AuthService(UserMapper userMapper, WalletMapper walletMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.walletMapper = walletMapper;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Map<String, Object> register(String username, String password, String nickname) {
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        // P1-7 修复：密码复杂度验证
        validatePasswordStrength(password);

        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname);
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(java.math.BigDecimal.ZERO);
        walletMapper.insert(wallet);

        return buildLoginResult(user);
    }

    public Map<String, Object> login(String username, String password) {
        // P1-5 修复：检查登录尝试次数
        checkLoginAttempts(username);

        User user = userMapper.findByUsername(username);
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            // 记录失败尝试
            recordFailedAttempt(username);
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 登录成功，清除失败记录
        loginAttempts.remove(username);

        return buildLoginResult(user);
    }

    /**
     * P1-5 修复：检查登录尝试次数，防止暴力破解
     */
    private void checkLoginAttempts(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt != null) {
            long now = System.currentTimeMillis();
            if (now - attempt.lastAttemptTime < LOCKOUT_DURATION && attempt.count.get() >= MAX_ATTEMPTS) {
                long remainingMinutes = (LOCKOUT_DURATION - (now - attempt.lastAttemptTime)) / 60000;
                throw new BusinessException(429, "登录失败次数过多，请 " + remainingMinutes + " 分钟后再试");
            }
            // 如果锁定期过了，重置计数
            if (now - attempt.lastAttemptTime >= LOCKOUT_DURATION) {
                loginAttempts.remove(username);
            }
        }
    }

    /**
     * P1-5 修复：记录失败的登录尝试
     */
    private void recordFailedAttempt(String username) {
        long now = System.currentTimeMillis();
        loginAttempts.compute(username, (key, attempt) -> {
            if (attempt == null) {
                return new LoginAttempt(1, now);
            }
            // 如果距离上次尝试超过锁定期，重置计数
            if (now - attempt.lastAttemptTime >= LOCKOUT_DURATION) {
                return new LoginAttempt(1, now);
            }
            attempt.count.incrementAndGet();
            attempt.lastAttemptTime = now;
            return attempt;
        });
    }

    /**
     * P1-5 修复：登录尝试记录
     */
    private static class LoginAttempt {
        final AtomicInteger count;
        long lastAttemptTime;

        LoginAttempt(int count, long lastAttemptTime) {
            this.count = new AtomicInteger(count);
            this.lastAttemptTime = lastAttemptTime;
        }
    }

    private Map<String, Object> buildLoginResult(User user) {
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", user);
        return map;
    }

    public User profile() {
        Long userId = UserContext.getUserId();
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    public void updateProfile(User req) {
        Long userId = UserContext.getUserId();
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setNickname(req.getNickname());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setAvatar(req.getAvatar());
        userMapper.update(user);
    }

    public void changePassword(String oldPassword, String newPassword) {
        Long userId = UserContext.getUserId();
        User user = userMapper.findById(userId);
        if (user == null || !BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }

        // P1-7 修复：新密码复杂度验证
        validatePasswordStrength(newPassword);

        userMapper.updatePassword(userId, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
    }

    /**
     * P1-7 修复：密码强度验证
     * 要求：至少8位，包含大写字母、小写字母、数字
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException(400, "密码长度至少8位");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new BusinessException(400, "密码必须包含至少一个大写字母");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new BusinessException(400, "密码必须包含至少一个小写字母");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new BusinessException(400, "密码必须包含至少一个数字");
        }

        // 可选：检查常见弱密码
        String[] weakPasswords = {"12345678", "Password1", "Qwerty123", "Admin123"};
        for (String weak : weakPasswords) {
            if (password.equalsIgnoreCase(weak)) {
                throw new BusinessException(400, "密码过于简单，请使用更复杂的密码");
            }
        }
    }
}
