package com.company.decentralized_disaster_relief.auth_service.controller;

import com.company.decentralized_disaster_relief.auth_service.dto.*;
import com.company.decentralized_disaster_relief.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final AuthService authService;

    @Value("${app.base-url:http://localhost:8090}")
    private String appBaseUrl;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequest request) {
        log.info("Signup request for email={}", request.getEmail());
        String verifyUrl = authService.signup(request, appBaseUrl);
        log.info("Verification link (simulated email): {}", verifyUrl);
        return ResponseEntity.ok(Map.of("message", "verification_sent", "verifyUrl", verifyUrl));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam("token") String token) {
        log.info("Verifying token={}", token);
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "email_verified"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("Password reset requested for email={}", request.getEmail());
        String resetUrl = authService.initiatePasswordReset(request, appBaseUrl);
        log.info("Password reset link (simulated email): {}", resetUrl);
        return ResponseEntity.ok(Map.of("message", "reset_initiated", "resetUrl", resetUrl));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Reset password using token={}", request.getToken());
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "password_reset"));
    }
}
