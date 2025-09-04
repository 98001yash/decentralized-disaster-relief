package com.company.decentralized_disaster_relief.api_gateway.filters;

import com.company.decentralized_disaster_relief.api_gateway.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    private final JwtService jwtService;

    @Value("${security.internal-gateway-secret:}")
    private String internalGatewaySecret;

    private static final Set<String> DEFAULT_PUBLIC_PATHS = Set.of(
            "/auth/login",
            "/auth/signup",
            "/auth/forgot-password",
            "auth/reset-password",
            "auth/verify/**",
            "/actuator/health",
            "/actuator/info"
    );

    public AuthenticationFilter(JwtService jwtService) {
        super(NameConfig.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            HttpMethod method = exchange.getRequest().getMethod();

            // Allow CORS preflight through
            if (HttpMethod.OPTIONS.equals(method)) {
                return chain.filter(exchange);
            }

            // Skip public paths
            if (isPublicPath(path)) {
                log.debug("Public path, skipping authentication: {}", path);
                return chain.filter(exchange);
            }

            final String tokenHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                log.warn("Authorization header missing or invalid for path {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            final String token = tokenHeader.substring(7).trim();

            try {
                // Use your JwtService to validate & extract claims
                // JwtService should throw JwtException for invalid/expired tokens
                Claims claims = jwtService.extractAllClaims(token);

                // Extract user id (subject or custom claim)
                String userId = extractUserIdFromClaims(claims);
                if (userId == null || userId.isBlank()) {
                    log.warn("User id claim missing in token for path {}", path);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Extract roles: try "roles" claim (could be List or String). Fallback to "authorities".
                String rolesHeader = extractRolesHeaderFromClaims(claims);

                // Build modified exchange with headers forwarded
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> {
                            r.header("X-User-Id", userId);
                            if (rolesHeader != null && !rolesHeader.isBlank()) {
                                r.header("X-Roles", rolesHeader);
                            }
                            if (internalGatewaySecret != null && !internalGatewaySecret.isBlank()) {
                                r.header("X-Internal-Auth", internalGatewaySecret);
                            }
                        })
                        .build();

                log.debug("Authenticated user ID: {} roles: {} path: {}", userId, rolesHeader, path);
                return chain.filter(modifiedExchange);

            } catch (JwtException e) {
                log.warn("JWT validation error for path {}: {}", path, e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (Exception ex) {
                // Defensive catch: do not leak internal errors
                log.error("Unexpected error in authentication filter for path {}: {}", path, ex.getMessage(), ex);
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private boolean isPublicPath(String path) {
        if (DEFAULT_PUBLIC_PATHS.contains(path)) return true;
        return path.startsWith("/public/") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs");
    }

    private String extractUserIdFromClaims(Claims claims) {
        // Common places: subject (sub) or custom claim "userId"
        if (claims.getSubject() != null) {
            return claims.getSubject();
        }
        Object uid = claims.get("userId");
        if (uid != null) return uid.toString();
        // fallback
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractRolesHeaderFromClaims(Claims claims) {

        Object rolesObj = claims.get("roles");
        if (rolesObj == null) rolesObj = claims.get("authorities");

        if (rolesObj == null) return null;

        if (rolesObj instanceof List<?> list) {
            List<String> rolesList = list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            return String.join(",", rolesList);
        } else {
            // single string (maybe comma-separated)
            return rolesObj.toString();
        }
    }

    public static class Config {
        // left intentionally empty
    }
}
