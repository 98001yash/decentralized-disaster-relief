package com.company.decentralized_disaster_relief.auth_service.service.Impl;

import com.company.decentralized_disaster_relief.auth_service.dto.*;
import com.company.decentralized_disaster_relief.auth_service.entity.PasswordResetToken;
import com.company.decentralized_disaster_relief.auth_service.entity.User;
import com.company.decentralized_disaster_relief.auth_service.entity.VerificationToken;
import com.company.decentralized_disaster_relief.auth_service.repository.PasswordResetTokenRepository;
import com.company.decentralized_disaster_relief.auth_service.repository.UserRepository;
import com.company.decentralized_disaster_relief.auth_service.repository.VerificationTokenRepository;
import com.company.decentralized_disaster_relief.auth_service.security.JwtService;
import com.company.decentralized_disaster_relief.auth_service.service.AuthService;
import com.company.decentralized_disaster_relief.auth_service.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void signup(SignupRequest request, String frontendBaseUrl) {

        String email = request.getEmail().toLowerCase(Locale.ROOT).trim();
        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("Email already registered!");
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .roles(new HashSet<>(Collections.singleton("ROLE_USER")))
                .provider("LOCAL")
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);

        // remove existing tokens for user in case
        verificationTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .createdAt(Instant.now())
                .build();
        verificationTokenRepository.save(vt);

        String verifyUrl = String.format("%s/api/auth/verify?token=%s", frontendBaseUrl, token);
        emailService.sendVerificationEmail(email, verifyUrl);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(()->new IllegalArgumentException("Invalid verification token"));

        if(vt.getExpiryAt().isBefore(Instant.now())){
            throw new IllegalArgumentException("Verification token expired");
        }

        User user = vt.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.deleteByUserId(user.getId());

    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase(Locale.ROOT).trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Email not verified");
        }
        if (user.isOauthUser()) {
            throw new IllegalArgumentException("User registered via OAuth, please login with OAuth provider.");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        // Generate JWT
        String token = jwtService.generateAccessToken(user);

        // hardcoded to 10 min because your JwtService uses .setExpiration(10 minutes)
        long expiresInMs = 1000L * 60L * 10L;

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .expiresInMs(expiresInMs)
                .roles(user.getRoles())
                .build();
    }

    @Override
    public void initiatePasswordReset(ForgotPasswordRequest req, String frontendBaseUrl) {

        String email = req.getEmail().toLowerCase(Locale.ROOT).trim();

        Optional<User> maybe = userRepository.findByEmail(email);
        if(maybe.isEmpty()){
            return;
        }
        User user = maybe.get();

        passwordResetTokenRepository.deleteAllByUserId(user.getId());
        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .used(false)
                .createdAt(Instant.now())
                .build();
        passwordResetTokenRepository.save(prt);

        String resetUrl = String.format("%s/reset-password?token=%s", frontendBaseUrl, token);
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(req.getToken())
                .orElseThrow(()->new IllegalArgumentException("Invalid reset token"));

        if (prt.isUsed()) throw new IllegalArgumentException("Reset token already used");
        if (prt.getExpiryAt().isBefore(Instant.now())) throw new IllegalArgumentException("Reset token expired");

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
        passwordResetTokenRepository.deleteAllByUserId(user.getId());
    }
}
