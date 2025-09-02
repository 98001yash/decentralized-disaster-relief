package com.company.decentralized_disaster_relief.auth_service.service;

import com.company.decentralized_disaster_relief.auth_service.dto.*;

public interface AuthService {

    String signup(SignupRequest request, String frontendBaseUrl);

    void verifyEmail(String token);

    AuthResponse login(LoginRequest request);
    String initiatePasswordReset(ForgotPasswordRequest req, String frontendBaseUrl);
    void resetPassword(ResetPasswordRequest req);
}
