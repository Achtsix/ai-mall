package com.aimall.controller;

import com.aimall.common.Result;
import com.aimall.entity.User;
import com.aimall.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> req) {
        return Result.ok(authService.register(req.get("username"), req.get("password"), req.get("nickname")));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> req) {
        return Result.ok(authService.login(req.get("username"), req.get("password")));
    }

    @GetMapping("/profile")
    public Result<User> profile() {
        return Result.ok(authService.profile());
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User user) {
        authService.updateProfile(user);
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> req) {
        authService.changePassword(req.get("oldPassword"), req.get("newPassword"));
        return Result.ok();
    }
}
