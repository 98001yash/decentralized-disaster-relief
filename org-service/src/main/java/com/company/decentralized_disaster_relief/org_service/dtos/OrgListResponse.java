package com.company.decentralized_disaster_relief.org_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgListResponse {

    private List<OrgResponse> items;
    private Long total;
    private int page;
    private int size;
}
