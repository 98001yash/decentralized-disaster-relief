package com.company.decentralized_disaster_relief.org_service.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class AuthFlags {

    public static Long requireUserId() {
        Long id = UserContextHolder.getCurrentUserId();
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return id;
    }

    public static boolean isPlatformAdmin() {
        return UserContextHolder.hasRole("ROLE_ADMIN");
    }

    public static boolean isPlatformVerifier() {
        return UserContextHolder.hasRole("ROLE_VERIFIER");
    }
}
