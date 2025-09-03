package com.company.decentralized_disaster_relief.org_service.repository;

import com.company.decentralized_disaster_relief.org_service.entity.Org;
import com.company.decentralized_disaster_relief.org_service.enums.OrgStatus;
import com.company.decentralized_disaster_relief.org_service.enums.OrgType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgRepository extends JpaRepository<Org,Long> {


    Page<Org> findByStatus(OrgStatus status, Pageable pageable);

    Page<Org> findByCreatedByUserId(Long createdByUserId, Pageable pageable);

    Page<Org> findByTypeAndStatus(OrgType type, OrgStatus status, Pageable pageable);

    Page<Org> findByNameIgnoreCaseContaining(String name, Pageable pageable);


    List<Org> findByVerifiedTrue();
}
