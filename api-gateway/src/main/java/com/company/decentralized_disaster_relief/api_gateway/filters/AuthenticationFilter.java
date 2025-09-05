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
            "/actuator/health",
            "/actuator/info",
            "auth/forgot-password",
            "auth/reset-password",
            "auth/verify/**"
    );

    public AuthenticationFilter(JwtService jwtService) {
        super(NameConfig.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // allow CORS preflight
            if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
                return chain.filter(exchange);
            }

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
                Claims claims = jwtService.extractAllClaims(token); // must return io.jsonwebtoken.Claims

                String userId = extractUserIdFromClaims(claims);
                if (userId == null || userId.isBlank()) {
                    log.warn("User id missing in token for path {}", path);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String rolesHeader = extractRolesHeaderFromClaims(claims); // e.g. "ROLE_USER,ROLE_ADMIN"

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

                log.debug("Authenticated userId={} roles={} path={}", userId, rolesHeader, path);
                return chain.filter(modifiedExchange);

            } catch (JwtException e) {
                log.warn("JWT validation error for path {}: {}", path, e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (Exception ex) {
                log.error("Unexpected error in auth filter for path {}: {}", path, ex.getMessage(), ex);
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
        if (claims.getSubject() != null) return claims.getSubject();
        Object uid = claims.get("userId");
        return uid == null ? null : uid.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractRolesHeaderFromClaims(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (rolesObj == null) rolesObj = claims.get("authorities");

        if (rolesObj == null) return null;

        if (rolesObj instanceof List<?> list) {
            List<String> roles = list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            return String.join(",", roles);
        } else {
            return rolesObj.toString();
        }
    }

    public static class Config {
        // empty
    }
}
