package com.company.decentralized_disaster_relief.org_service.dtos;


import com.company.decentralized_disaster_relief.org_service.enums.OrgMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgMemberCreateRequest {

    private Long userId;
    private OrgMemberRole role;
}
