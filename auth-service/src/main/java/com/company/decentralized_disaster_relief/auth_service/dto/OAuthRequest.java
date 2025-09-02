package com.company.decentralized_disaster_relief.auth_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuthRequest {

    private String provider;   // GOOGLE, FACEBOOK
    private String providerId; // unique id provider
    private String email;
}
