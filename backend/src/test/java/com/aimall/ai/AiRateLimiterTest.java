package com.aimall.ai;

import com.aimall.common.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiRateLimiterTest {

    @Test
    void rejectsRequestsAboveConfiguredLimit() {
        AiRateLimiter limiter = new AiRateLimiter(2);

        assertDoesNotThrow(() -> limiter.check(7L));
        assertDoesNotThrow(() -> limiter.check(7L));
        BusinessException exception = assertThrows(BusinessException.class, () -> limiter.check(7L));

        assertEquals(429, exception.getCode());
    }
}
