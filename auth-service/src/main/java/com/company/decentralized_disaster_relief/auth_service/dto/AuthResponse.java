package com.company.decentralized_disaster_relief.auth_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String email;
    private boolean enabled;
    private Long expiresInMs;
    private Set<String> roles;
}
