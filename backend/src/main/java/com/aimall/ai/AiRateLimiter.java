package com.aimall.ai;

import com.aimall.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiRateLimiter {

    private final ConcurrentHashMap<Long, Deque<Long>> requests = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public AiRateLimiter(@Value("${aimall.agent.requests-per-minute:12}") int requestsPerMinute) {
        this.requestsPerMinute = Math.max(1, requestsPerMinute);
    }

    public void check(Long userId) {
        if (userId == null) throw new BusinessException(401, "请先登录");
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000;
        Deque<Long> userRequests = requests.computeIfAbsent(userId, ignored -> new ArrayDeque<>());
        synchronized (userRequests) {
            while (!userRequests.isEmpty() && userRequests.peekFirst() < windowStart) userRequests.removeFirst();
            if (userRequests.size() >= requestsPerMinute) {
                throw new BusinessException(429, "AI 请求过于频繁，请一分钟后再试");
            }
            userRequests.addLast(now);
        }
    }
}
