package com.company.decentralized_disaster_relief.org_service.entity;


import com.company.decentralized_disaster_relief.org_service.enums.OrgMemberRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
@Entity
@Table(
        name = "org_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_org_members_org_user", columnNames = {"org_id", "userId"})
        },
        indexes = {
                @Index(name = "idx_org_members_org", columnList = "org_id"),
                @Index(name = "idx_org_members_user", columnList = "userId")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    @Builder.Default
    private OrgMemberRole role = OrgMemberRole.ORG_MEMBER;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt;


    @PrePersist
    protected void onCreate(){
        this.joinedAt = Instant.now();
    }
}
