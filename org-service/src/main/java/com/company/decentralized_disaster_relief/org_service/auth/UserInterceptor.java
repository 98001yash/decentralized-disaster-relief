package com.company.decentralized_disaster_relief.org_service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Value("${security.internal-gateway-secret:}")
    private String internalGatewaySecret;

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLES = "X-Roles";
    private static final String HEADER_INTERNAL = "X-Internal-Auth";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Optional enforcement: only accept header-set requests from trusted gateway
        if (internalGatewaySecret != null && !internalGatewaySecret.isBlank()) {
            String internal = request.getHeader(HEADER_INTERNAL);
            if (!internalGatewaySecret.equals(internal)) {
                log.warn("Missing or invalid internal gateway header; rejecting request");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }

        String userIdHdr = request.getHeader(HEADER_USER_ID);
        if (userIdHdr != null && !userIdHdr.isBlank()) {
            try {
                Long userId = Long.valueOf(userIdHdr.trim());
                UserContextHolder.setCurrentUserId(userId);
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header: {}", userIdHdr);
            }
        }

        String rolesHdr = request.getHeader(HEADER_ROLES);
        if (rolesHdr != null && !rolesHdr.isBlank()) {
            UserContextHolder.setCurrentRolesFromHeader(rolesHdr);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
