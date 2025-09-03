package com.company.decentralized_disaster_relief.org_service.dtos;


import com.company.decentralized_disaster_relief.org_service.enums.OrgType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgCreateRequest {

    private String name;
    private OrgType type;
    private String registrationNumber;
    private String contactEmail;
    private String contactPhone;
    private String address;
}
