package com.company.decentralized_disaster_relief.auth_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "verification_tokens",
        indexes = {
                @Index(name = "idx_verification_token_token", columnList = "token")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Instant expiryAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
