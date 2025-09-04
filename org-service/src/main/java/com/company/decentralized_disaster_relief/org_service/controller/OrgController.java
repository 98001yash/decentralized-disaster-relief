package com.company.decentralized_disaster_relief.org_service.controller;


import com.company.decentralized_disaster_relief.org_service.auth.AuthFlags;
import com.company.decentralized_disaster_relief.org_service.dtos.OrgCreateRequest;
import com.company.decentralized_disaster_relief.org_service.dtos.OrgResponse;
import com.company.decentralized_disaster_relief.org_service.service.OrgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
