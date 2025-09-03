package com.company.decentralized_disaster_relief.org_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgVerifyRequest {

    private String verificationLevel;
    private String notes;
}
