package com.hustsimulator.context.config;

import com.hustsimulator.context.interceptor.RoleInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply RBAC to all relevant API endpoints
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/buildings/**", "/api/events/**", "/api/recurring-events/**", "/api/rooms/**", "/api/maps/**");
    }
}
