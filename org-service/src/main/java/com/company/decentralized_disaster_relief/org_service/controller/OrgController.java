package com.company.decentralized_disaster_relief.org_service.controller;


import com.company.decentralized_disaster_relief.org_service.auth.AuthFlags;
import com.company.decentralized_disaster_relief.org_service.dtos.*;
import com.company.decentralized_disaster_relief.org_service.repository.OrgRepository;
import com.company.decentralized_disaster_relief.org_service.service.OrgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/org")
@Slf4j
public class OrgController {

    private final OrgService orgService;

    @PostMapping
    public ResponseEntity<OrgResponse> createOrg(@RequestBody OrgCreateRequest req){
        Long creatorUserId = AuthFlags.requireUserId();
        OrgResponse created = orgService.createOrg(req, creatorUserId);
        log.info("Org created id={} by userId={}", created.getId(), creatorUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrgResponse> getOrg(@PathVariable("id") Long id){
        OrgResponse resp = orgService.getOrg(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<Page<OrgResponse>> listOrgs(
            @RequestParam(value = "q", required = false) String q,
            @PageableDefault(size = 20) Pageable pageable){
         Page<OrgResponse> page = orgService.listOrgs(q, pageable);
         return ResponseEntity.ok(page);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrgResponse> updateOrg(
            @PathVariable("id") Long id,
            @RequestBody OrgUpdateRequest req
            ){
        Long requester = AuthFlags.requireUserId();
        boolean isPlatformAdmin = AuthFlags.isPlatformAdmin();
        OrgResponse updated = orgService.updateOrg(id, req, requester, isPlatformAdmin);
        log.info("Org updated id={} by userId={}", id, requester);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<OrgMemberResponse> addMember(
            @PathVariable("id") Long id,
             @RequestBody OrgMemberCreateRequest req) {

        Long requester = AuthFlags.requireUserId();
        boolean isPlatformAdmin = AuthFlags.isPlatformAdmin();
        OrgMemberResponse member = orgService.addMember(id, req, requester, isPlatformAdmin);
        log.info("Added member userId={} role={} to orgId={} by requester={}", member.getUserId(), member.getRole(), id, requester);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<OrgMemberResponse>> listMembers(@PathVariable("id") Long id) {
        List<OrgMemberResponse> members = orgService.listMembers(id);
        return ResponseEntity.ok(members);
    }


    @PostMapping("/{id}/verify")
    public ResponseEntity<OrgResponse> verifyOrg(
            @PathVariable("id") Long id,
            @RequestBody(required = false) OrgVerifyRequest verifyRequest) {

        Long verifierUserId = AuthFlags.requireUserId();
        boolean canVerify = AuthFlags.isPlatformVerifier() || AuthFlags.isPlatformAdmin();
        OrgResponse resp = orgService.verifyOrg(id, verifyRequest, verifierUserId, canVerify);
        log.info("Org id={} verified by userId={}", id, verifierUserId);
        return ResponseEntity.ok(resp);
    }


    @PostMapping("/{id}/reject")
    public ResponseEntity<OrgResponse> rejectOrg(
            @PathVariable("id") Long id,
            @RequestBody OrgRejectRequest rejectRequest) {

        Long verifierUserId = AuthFlags.requireUserId();
        boolean canVerify = AuthFlags.isPlatformVerifier() || AuthFlags.isPlatformAdmin();
        OrgResponse resp = orgService.rejectOrg(id, rejectRequest, verifierUserId, canVerify);
        log.info("Org id={} rejected by userId={} reason={}", id, verifierUserId, rejectRequest.getReason());
        return ResponseEntity.ok(resp);
    }

}
