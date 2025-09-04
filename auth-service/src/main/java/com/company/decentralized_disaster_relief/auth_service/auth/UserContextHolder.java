package com.company.decentralized_disaster_relief.auth_service.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserContextHolder {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> CURRENT_ROLES = new ThreadLocal<>();


    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    static void setCurrentUserId(Long userId) {
        if (userId == null) {
            CURRENT_USER_ID.remove();
        } else {
            CURRENT_USER_ID.set(userId);
        }
    }

    public static Set<String> getCurrentRoles() {
        Set<String> roles = CURRENT_ROLES.get();
        return roles == null ? Collections.emptySet() : Collections.unmodifiableSet(roles);
    }


    static void setCurrentRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            CURRENT_ROLES.remove();
        } else {
            CURRENT_ROLES.set(new HashSet<>(roles));
        }
    }

    static void setCurrentRolesFromHeader(String commaSeparatedRoles) {
        if (commaSeparatedRoles == null || commaSeparatedRoles.isBlank()) {
            CURRENT_ROLES.remove();
            return;
        }
        Set<String> roles = Arrays.stream(commaSeparatedRoles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        if (roles.isEmpty()) {
            CURRENT_ROLES.remove();
        } else {
            CURRENT_ROLES.set(roles);
        }
    }

    public static boolean hasRole(String role) {
        if (role == null || role.isBlank()) return false;
        return getCurrentRoles().contains(role);
    }

    // ---------- cleanup ----------
    static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_ROLES.remove();
    }
}
