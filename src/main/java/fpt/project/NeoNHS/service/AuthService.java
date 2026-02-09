package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.auth.ChangePasswordRequest;
import fpt.project.NeoNHS.dto.request.auth.LoginRequest;
import fpt.project.NeoNHS.dto.request.auth.RegisterRequest;
import fpt.project.NeoNHS.dto.response.AuthResponse;
import fpt.project.NeoNHS.dto.response.auth.UserInfoResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);
    void changePassword(String email, ChangePasswordRequest request);

    AuthResponse googleLogin(String idToken);

    void sendTestEmail();

    void verifyOtp(String email, String otp);

    void sendVerifyEmail(String email);

    void sendResetPasswordOtp(String email);

    void resetPassword(String email, String newPassword, String confirmPassword);

    UserInfoResponse getCurrentUser(String email);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}
