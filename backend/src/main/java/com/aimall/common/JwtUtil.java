package com.aimall.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${aimall.jwt.secret}")
    private String secret;

    @Value("${aimall.jwt.expire-hours:72}")
    private long expireHours;

    public String createToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expire = DateUtil.offsetHour(now, (int) expireHours);
        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("username", username)
                .setPayload("role", role)
                .setIssuedAt(now)
                .setExpiresAt(expire)
                .setKey(secret.getBytes(StandardCharsets.UTF_8))
                .sign();
    }

    public boolean verify(String token) {
        try {
            return JWTUtil.verify(token, secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    public JWT parse(String token) {
        return JWTUtil.parseToken(token);
    }
}
