package com.company.decentralized_disaster_relief.auth_service.repository;

import com.company.decentralized_disaster_relief.auth_service.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Long> {

    void deleteByUserId(Long userId);

    Optional<VerificationToken> findByUserId(Long userId);
}
