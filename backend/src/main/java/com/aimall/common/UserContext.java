package com.aimall.common;

public class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static String getRole() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getRole();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static class LoginUser {
        private Long userId;
        private String username;
        private String role;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
