package com.company.decentralized_disaster_relief.org_service.dtos;


import com.company.decentralized_disaster_relief.org_service.enums.OrgStatus;
import com.company.decentralized_disaster_relief.org_service.enums.OrgType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgResponse {

    private Long id;
    private String name;
    private OrgType type;
    private String registrationNumber;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private OrgStatus status;
    private boolean verified;
    private String verificationLevel;
    private Long verificationByUserId;
    private Instant verifiedAt;
    private String verificationNotes;
    private Long createdByUserId;
    private Long updatedByUserId;
    private Instant createdAt;
    private Instant updatedAt;
}
