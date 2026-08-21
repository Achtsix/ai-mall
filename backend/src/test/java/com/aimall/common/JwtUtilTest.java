package com.aimall.common;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    @Test
    void acceptsSignedActiveTokenAndRejectsExpiredToken() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "benchmark-secret-that-is-long-enough");
        ReflectionTestUtils.setField(jwtUtil, "expireHours", 1L);
        assertTrue(jwtUtil.verify(jwtUtil.createToken(7L, "tester", "USER")));

        ReflectionTestUtils.setField(jwtUtil, "expireHours", -1L);
        assertFalse(jwtUtil.verify(jwtUtil.createToken(7L, "tester", "USER")));
    }
}
