package com.company.decentralized_disaster_relief.org_service.controller;


import com.company.decentralized_disaster_relief.org_service.auth.AuthFlags;
import com.company.decentralized_disaster_relief.org_service.dtos.OrgCreateRequest;
import com.company.decentralized_disaster_relief.org_service.dtos.OrgResponse;
import com.company.decentralized_disaster_relief.org_service.dtos.OrgUpdateRequest;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/org")
@Slf4j
public class OrgController {

    private final OrgService orgService;
    private final OrgRepository orgRepository;

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
}
