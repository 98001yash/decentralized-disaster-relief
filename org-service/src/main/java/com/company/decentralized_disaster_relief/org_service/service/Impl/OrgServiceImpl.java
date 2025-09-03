package com.company.decentralized_disaster_relief.org_service.service.Impl;

import com.company.decentralized_disaster_relief.org_service.dtos.*;
import com.company.decentralized_disaster_relief.org_service.entity.Org;
import com.company.decentralized_disaster_relief.org_service.entity.OrgMember;
import com.company.decentralized_disaster_relief.org_service.enums.OrgMemberRole;
import com.company.decentralized_disaster_relief.org_service.enums.OrgStatus;
import com.company.decentralized_disaster_relief.org_service.enums.OrgType;
import com.company.decentralized_disaster_relief.org_service.exceptions.BadRequestException;
import com.company.decentralized_disaster_relief.org_service.exceptions.ResourceNotFoundException;
import com.company.decentralized_disaster_relief.org_service.repository.OrgMemberRepository;
import com.company.decentralized_disaster_relief.org_service.repository.OrgRepository;
import com.company.decentralized_disaster_relief.org_service.service.OrgService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrgServiceImpl implements OrgService {

    private final OrgRepository orgRepository;
    private final OrgMemberRepository orgMemberRepository;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public OrgResponse createOrg(OrgCreateRequest req, Long creatorUserId) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BadRequestException("Organization name is required");
        }

        Org org = Org.builder()
                .name(req.getName().trim())
                .type(req.getType() == null ? OrgType.NGO : req.getType())
                .registrationNumber(req.getRegistrationNumber())
                .contactEmail(req.getContactEmail())
                .contactPhone(req.getContactPhone())
                .address(req.getAddress())
                .status(OrgStatus.PENDING)
                .createdByUserId(creatorUserId)
                .updatedByUserId(creatorUserId)
                .build();

        org = orgRepository.save(org);

        // add creator as ORG_ADMIN
        OrgMember member = OrgMember.builder()
                .org(org)
                .userId(creatorUserId)
                .role(OrgMemberRole.ORG_ADMIN)
                .build();
        orgMemberRepository.save(member);

        log.info("Org created id={} by userId={}", org.getId(), creatorUserId);

        return modelMapper.map(org, OrgResponse.class);
    }

    @Override
    public OrgResponse getOrg(Long orgId) {
        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));
        return modelMapper.map(org, OrgResponse.class);
    }

    @Override
    public Page<OrgResponse> listOrgs(String nameFilter, Pageable pageable) {
        Page<Org> page;
        if (nameFilter == null || nameFilter.isBlank()) {
            page = orgRepository.findAll(pageable);
        } else {
            page = orgRepository.findByNameIgnoreCaseContaining(nameFilter, pageable);
        }
        return page.map(org -> modelMapper.map(org, OrgResponse.class));
    }

    @Override
    @Transactional
    public OrgResponse updateOrg(Long orgId, OrgUpdateRequest req, Long requesterUserId, boolean isPlatformAdmin) {
        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));

        if (!isPlatformAdmin && !isOrgAdmin(orgId, requesterUserId)) {
            throw new BadRequestException("Only org-admin or platform-admin can update organization");
        }

        // Map non-null fields from req to entity (ModelMapper configured to skip nulls)
        modelMapper.map(req, org);
        org.setUpdatedByUserId(requesterUserId);
        org = orgRepository.save(org);
        log.info("Org updated id={} by userId={}", org.getId(), requesterUserId);
        return modelMapper.map(org, OrgResponse.class);
    }

    @Override
    @Transactional
    public OrgMemberResponse addMember(Long orgId, OrgMemberCreateRequest req, Long requesterUserId, boolean isPlatformAdmin) {
        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));

        if (!isPlatformAdmin && !isOrgAdmin(orgId, requesterUserId)) {
            throw new BadRequestException("Only org-admin or platform-admin can add members");
        }

        if (orgMemberRepository.existsByOrgIdAndUserId(orgId, req.getUserId())) {
            throw new BadRequestException("User is already a member of this org");
        }

        OrgMember member = OrgMember.builder()
                .org(org)
                .userId(req.getUserId())
                .role(req.getRole())
                .build();
        member = orgMemberRepository.save(member);

        log.info("Added member userId={} as {} to orgId={} by requester={}", req.getUserId(), req.getRole(), orgId, requesterUserId);

        return toMemberResponse(member);
    }

    @Override
    public List<OrgMemberResponse> listMembers(Long orgId) {
        // ensure org exists
        if (!orgRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found: " + orgId);
        }
        List<OrgMember> members = orgMemberRepository.findByOrgId(orgId);
        return members.stream().map(this::toMemberResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrgResponse verifyOrg(Long orgId, OrgVerifyRequest verifyRequest, Long verifierUserId, boolean isPlatformVerifierOrAdmin) {
        if (!isPlatformVerifierOrAdmin) {
            throw new BadRequestException("Only platform verifier or admin can verify organizations");
        }

        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));

        org.setStatus(OrgStatus.VERIFIED);
        org.setVerified(true);
        org.setVerificationLevel(verifyRequest != null ? verifyRequest.getVerificationLevel() : null);
        org.setVerificationNotes(verifyRequest != null ? verifyRequest.getNotes() : null);
        org.setVerifiedByUserId(verifierUserId);
        org.setVerifiedAt(Instant.now());
        org.setUpdatedByUserId(verifierUserId);

        org = orgRepository.save(org);

        log.info("Organization id={} verified by userId={} level={}", orgId, verifierUserId, verifyRequest != null ? verifyRequest.getVerificationLevel() : null);

        return modelMapper.map(org, OrgResponse.class);
    }

    @Override
    @Transactional
    public OrgResponse rejectOrg(Long orgId, OrgRejectRequest rejectRequest, Long verifierUserId, boolean isPlatformVerifierOrAdmin) {
        if (!isPlatformVerifierOrAdmin) {
            throw new BadRequestException("Only platform verifier or admin can reject organizations");
        }

        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));

        org.setStatus(OrgStatus.REJECTED);
        org.setVerified(false);
        String notes = "Rejected";
        if (rejectRequest != null) {
            notes = rejectRequest.getReason() + (rejectRequest.getNotes() != null ? " | " + rejectRequest.getNotes() : "");
            org.setVerificationNotes(notes);
        }
        org.setUpdatedByUserId(verifierUserId);
        org = orgRepository.save(org);

        log.info("Organization id={} rejected by userId={} notes={}", orgId, verifierUserId, notes);

        return modelMapper.map(org, OrgResponse.class);
    }

    /* --   helpers ---    */

    private OrgMemberResponse toMemberResponse(OrgMember m) {
        OrgMemberResponse r = new OrgMemberResponse();
        r.setId(m.getId());
        r.setOrgId(m.getOrg().getId());
        r.setUserId(m.getUserId());
        r.setRole(m.getRole());
        r.setJoinedAt(m.getJoinedAt());
        return r;
    }

    private boolean isOrgAdmin(Long orgId, Long userId) {
        if (userId == null) return false;
        return orgMemberRepository.findByOrgIdAndUserId(orgId, userId)
                .map(m -> m.getRole() == OrgMemberRole.ORG_ADMIN)
                .orElse(false);
    }
}