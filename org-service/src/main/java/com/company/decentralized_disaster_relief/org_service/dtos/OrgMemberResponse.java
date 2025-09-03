package com.company.decentralized_disaster_relief.org_service.dtos;


import com.company.decentralized_disaster_relief.org_service.enums.OrgMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgMemberResponse {


    private Long id;
    private Long orgId;
    private Long userId;
    private OrgMemberRole role;
    private Instant joinedAt;
}
