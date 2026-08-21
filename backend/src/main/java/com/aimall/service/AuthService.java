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

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final WalletMapper walletMapper;
    private final JwtUtil jwtUtil;

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
        User user = userMapper.findByUsername(username);
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }
        return buildLoginResult(user);
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
        userMapper.updatePassword(userId, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
    }
}
