package com.company.decentralized_disaster_relief.auth_service.repository;

import com.company.decentralized_disaster_relief.auth_service.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {

    Optional<PasswordResetToken> findByToken(String token);


    void deleteAllByUserId(Long userId);
}
