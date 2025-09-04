package com.company.decentralized_disaster_relief.org_service.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserContextHolder {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> CURRENT_ROLES = new ThreadLocal<>();

    private UserContextHolder() { /* utility */ }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    static void setCurrentUserId(Long userId) {
        if (userId == null) CURRENT_USER_ID.remove();
        else CURRENT_USER_ID.set(userId);
    }

    public static Set<String> getCurrentRoles() {
        Set<String> s = CURRENT_ROLES.get();
        return s == null ? Collections.emptySet() : Collections.unmodifiableSet(s);
    }

    static void setCurrentRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            CURRENT_ROLES.remove();
        } else {
            CURRENT_ROLES.set(new HashSet<>(roles));
        }
    }

    static void setCurrentRolesFromHeader(String commaSeparated) {
        if (commaSeparated == null || commaSeparated.isBlank()) {
            CURRENT_ROLES.remove();
            return;
        }
        Set<String> roles = Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .filter(r -> !r.isEmpty())
                .collect(Collectors.toSet());
        if (roles.isEmpty()) CURRENT_ROLES.remove();
        else CURRENT_ROLES.set(roles);
    }

    public static boolean hasRole(String role) {
        if (role == null || role.isBlank()) return false;
        return getCurrentRoles().contains(role);
    }

    static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_ROLES.remove();
    }
}
