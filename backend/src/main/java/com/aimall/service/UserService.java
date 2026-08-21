package com.aimall.service;

import com.aimall.common.BusinessException;
import com.aimall.entity.User;
import com.aimall.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public PageInfo<User> page(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.findAllUsers();
        if (keyword != null && !keyword.isBlank()) {
            users.removeIf(u -> u.getUsername() == null || !u.getUsername().contains(keyword));
        }
        return new PageInfo<>(users);
    }

    public void updateStatus(Long userId, Integer status) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.update(user);
    }

    public void resetPassword(Long userId, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        userMapper.updatePassword(userId, cn.hutool.crypto.digest.BCrypt.hashpw(newPassword, cn.hutool.crypto.digest.BCrypt.gensalt()));
    }
}
