package com.aimall.interceptor;

import com.aimall.common.BusinessException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/**
 * P1-4 修复：CSRF 保护拦截器
 * 使用双重提交 Cookie 模式保护状态变更操作
 */
@Component
public class CsrfInterceptor implements HandlerInterceptor {

    private static final String CSRF_TOKEN_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final SecureRandom random = new SecureRandom();

    // 需要 CSRF 保护的方法
    private static final Set<String> PROTECTED_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    // 不需要 CSRF 保护的路径
    private static final Set<String> EXCLUDED_PATHS = Set.of(
        "/api/auth/login",
        "/api/auth/register"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        // 只保护状态变更操作（POST/PUT/DELETE/PATCH）
        if (!PROTECTED_METHODS.contains(method)) {
            ensureCsrfToken(request, response);
            return true;
        }

        // 排除的路径不需要 CSRF 保护
        if (isExcludedPath(path)) {
            ensureCsrfToken(request, response);
            return true;
        }

        // 验证 CSRF Token
        String cookieToken = getCsrfTokenFromCookie(request);
        String headerToken = request.getHeader(CSRF_HEADER_NAME);

        if (cookieToken == null || headerToken == null || !cookieToken.equals(headerToken)) {
            throw new BusinessException(403, "CSRF token 验证失败");
        }

        return true;
    }

    /**
     * 确保响应中包含 CSRF Token Cookie
     */
    private void ensureCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        String existingToken = getCsrfTokenFromCookie(request);
        if (existingToken == null) {
            String newToken = generateToken();
            Cookie cookie = new Cookie(CSRF_TOKEN_NAME, newToken);
            cookie.setPath("/");
            cookie.setHttpOnly(false); // 前端需要读取
            cookie.setMaxAge(3600); // 1小时
            cookie.setSecure(false); // 生产环境应设为 true（HTTPS）
            response.addCookie(cookie);
        }
    }

    /**
     * 从 Cookie 中获取 CSRF Token
     */
    private String getCsrfTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CSRF_TOKEN_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 生成随机 CSRF Token
     */
    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 检查路径是否在排除列表中
     */
    private boolean isExcludedPath(String path) {
        for (String excluded : EXCLUDED_PATHS) {
            if (path.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }
}
