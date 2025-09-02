package com.company.decentralized_disaster_relief.auth_service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false)
    private boolean enabled = false; // email verified

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", length = 50)
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Column(length = 50)
    private String provider;

    @Column(length = 255)
    private String providerId;


    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // convenience helper
    public boolean isOauthUser(){
        return provider !=null && !"LOCAL".equalsIgnoreCase(provider);
    }
}
