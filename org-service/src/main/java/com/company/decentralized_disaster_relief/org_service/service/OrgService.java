package com.company.decentralized_disaster_relief.org_service.service;

import com.company.decentralized_disaster_relief.org_service.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrgService {



    OrgResponse createOrg(OrgCreateRequest req, Long creatorUserId);
    OrgResponse getOrg(Long orgId);
    Page<OrgResponse> listOrgs(String nameFilter, Pageable pageable);
    OrgResponse updateOrg(Long orgId, OrgUpdateRequest req, Long requesterUserId, boolean isPlatformAdmin);

    OrgMemberResponse addMember(Long orgId, OrgMemberCreateRequest req, Long requesterUserId, boolean isPlatformAdmin);

    List<OrgMemberResponse> listMembers(Long orgId);
    OrgResponse verifyOrg(Long orgId, OrgVerifyRequest verifyRequest, Long verifierUserId, boolean isPlatformVerifierOrAdmin);

    OrgResponse rejectOrg(Long orgId, OrgRejectRequest rejectRequest, Long verifierUserId, boolean isPlatformVerifierOrAdmin);


}
