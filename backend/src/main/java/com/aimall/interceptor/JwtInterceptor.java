package com.aimall.interceptor;

import cn.hutool.jwt.JWT;
import com.aimall.common.JwtUtil;
import com.aimall.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }
        String token = auth.substring(7);
        if (!jwtUtil.verify(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }
        JWT jwt = jwtUtil.parse(token);
        UserContext.LoginUser user = new UserContext.LoginUser();
        user.setUserId(((Number) jwt.getPayload("userId")).longValue());
        user.setUsername((String) jwt.getPayload("username"));
        user.setRole((String) jwt.getPayload("role"));
        String requestUri = request.getRequestURI();
        boolean adminOnly = requestUri.startsWith("/api/admin")
                || requestUri.startsWith("/api/ai/tools")
                || requestUri.startsWith("/api/ai/knowledge");
        if (adminOnly && !"ADMIN".equals(user.getRole())) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"无管理员权限\"}");
            return false;
        }
        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
