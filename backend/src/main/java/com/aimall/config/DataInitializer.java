package com.aimall.config;

import cn.hutool.crypto.digest.BCrypt;
import com.aimall.entity.User;
import com.aimall.entity.Wallet;
import com.aimall.mapper.UserMapper;
import com.aimall.mapper.WalletMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 开发环境初始化默认账号：admin / 123456，user / 123456
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final WalletMapper walletMapper;

    public DataInitializer(UserMapper userMapper, WalletMapper walletMapper) {
        this.userMapper = userMapper;
        this.walletMapper = walletMapper;
    }

    @Override
    public void run(String... args) {
        initUser("admin", "系统管理员", "ADMIN");
        initUser("user", "测试用户", "USER");
    }

    private void initUser(String username, String nickname, String role) {
        User user = userMapper.findByUsername(username);
        String hash = BCrypt.hashpw("123456", BCrypt.gensalt());
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(hash);
            user.setNickname(nickname);
            user.setRole(role);
            user.setStatus(1);
            userMapper.insert(user);
        } else {
            userMapper.updatePassword(user.getId(), hash);
        }
        Wallet wallet = walletMapper.findByUserId(user.getId());
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(user.getId());
            wallet.setBalance(new BigDecimal("1000"));
            walletMapper.insert(wallet);
        }
    }
}
