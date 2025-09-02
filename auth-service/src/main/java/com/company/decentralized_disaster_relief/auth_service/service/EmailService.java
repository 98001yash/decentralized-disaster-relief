package com.company.decentralized_disaster_relief.auth_service.service;


import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendVerificationEmail(String toEmail, String verificationUrl) {
        // replace with actual mail sender
        System.out.println("[EMAIL] Verification link for " + toEmail + " -> " + verificationUrl);
    }

    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        System.out.println("[EMAIL] Password reset link for " + toEmail + " -> " + resetUrl);
    }
}
