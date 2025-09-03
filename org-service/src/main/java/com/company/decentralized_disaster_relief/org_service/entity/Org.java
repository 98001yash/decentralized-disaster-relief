package com.company.decentralized_disaster_relief.org_service.entity;


import com.company.decentralized_disaster_relief.org_service.enums.OrgStatus;
import com.company.decentralized_disaster_relief.org_service.enums.OrgType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "orgs",
        indexes = {
                @Index(name = "idx_orgs_name", columnList = "name"),
                @Index(name = "idx_orgs_status", columnList = "status"),
                @Index(name = "idx_orgs_type", columnList = "type"),
                @Index(name = "idx_orgs_registration_number", columnList = "registrationNumber")
        }
)
@Builder
public class Org {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrgType type;

    @Column(length = 128)
    private String registrationNumber;

    @Column(length = 255)
    private String contactEmail;

    @Column(length = 32)
    private String contactPhone;

    @Column(columnDefinition = "text")
    private String address;

    // Verification state machine
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private OrgStatus status = OrgStatus.PENDING;

    @Column(nullable =false)
    @Builder.Default
    private boolean verified = false;


    @Column(length = 20)
    private String verificationLevel;

    private Long verifiedByUserId;
    private Long updatedByUserId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        this.verified = (this.status ==OrgStatus.VERIFIED);
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = Instant.now();
        this.verified = (this.status ==OrgStatus.VERIFIED);
    }
}
