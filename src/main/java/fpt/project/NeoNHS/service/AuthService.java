package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.auth.LoginRequest;
import fpt.project.NeoNHS.dto.request.auth.RegisterRequest;
import fpt.project.NeoNHS.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse googleLogin(String idToken);

    void sendTestEmail();

    void verifyOtp(String email, String otp);

    void sendVerifyEmail(String email);

    void sendResetPasswordOtp(String email);

    void resetPassword(String email, String newPassword, String confirmPassword);
}
