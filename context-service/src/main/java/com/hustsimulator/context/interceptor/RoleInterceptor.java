package com.hustsimulator.context.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        
        // We only restrict POST, PUT, PATCH, DELETE operations
        if (HttpMethod.POST.name().equalsIgnoreCase(method) ||
            HttpMethod.PUT.name().equalsIgnoreCase(method) ||
            HttpMethod.PATCH.name().equalsIgnoreCase(method) ||
            HttpMethod.DELETE.name().equalsIgnoreCase(method)) {
            
            String userRole = request.getHeader("X-User-Role");
            
            if (userRole == null || !userRole.equalsIgnoreCase("ADMIN")) {
                log.warn("Access denied. User role '{}' is not authorized to perform {} on {}", userRole, method, request.getRequestURI());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only ADMIN can perform modifications.");
                return false; // Block the request
            }
        }
        
        // Allow all GET/OPTIONS/HEAD requests and requests with ADMIN role
        return true;
    }
}
