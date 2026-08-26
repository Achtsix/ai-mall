package com.aimall.config;

import com.aimall.interceptor.CsrfInterceptor;
import com.aimall.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final CsrfInterceptor csrfInterceptor;

    @Value("${aimall.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${aimall.upload.url-prefix:/uploads}")
    private String uploadUrlPrefix;

    public WebMvcConfig(JwtInterceptor jwtInterceptor, CsrfInterceptor csrfInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
        this.csrfInterceptor = csrfInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // P1-4 修复：添加 CSRF 保护拦截器（在 JWT 之前执行）
        registry.addInterceptor(csrfInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/uploads/**");

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/product/**",
                        "/api/category/**",
                        "/api/brand/**",
                        "/uploads/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteUploadLocation = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(uploadUrlPrefix + "/**")
                .addResourceLocations(absoluteUploadLocation);
    }
}
