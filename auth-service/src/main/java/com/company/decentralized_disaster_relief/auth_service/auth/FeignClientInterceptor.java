package com.company.decentralized_disaster_relief.auth_service.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${security.internal-gateway-secret:}")
    private String internalGatewaySecret;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // Forward current user id
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId != null) {
            requestTemplate.header("X-User-Id", userId.toString());
        }

        // Forward roles (comma separated string)
        Set<String> roles = UserContextHolder.getCurrentRoles();
        if (roles != null && !roles.isEmpty()) {
            requestTemplate.header("X-Roles", String.join(",", roles));
        }

        // Optionally forward internal gateway secret
        if (internalGatewaySecret != null && !internalGatewaySecret.isBlank()) {
            requestTemplate.header("X-Internal-Auth", internalGatewaySecret);
        }
    }
}
