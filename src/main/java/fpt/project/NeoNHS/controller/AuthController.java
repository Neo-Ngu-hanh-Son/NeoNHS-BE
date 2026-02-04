package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.ChangePasswordRequest;
import fpt.project.NeoNHS.dto.request.auth.LoginRequest;
import fpt.project.NeoNHS.dto.request.auth.RegisterRequest;
import fpt.project.NeoNHS.dto.request.auth.ForgotPasswordRequest;
import fpt.project.NeoNHS.dto.request.auth.ResetPasswordRequest;
import fpt.project.NeoNHS.dto.request.auth.VerifyOtpRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.AuthResponse;
import fpt.project.NeoNHS.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Login successful", data));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String refreshToken) {
        AuthResponse data = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Token refreshed successfully", data));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED,
                        "Registration successful, please check your email for verification", data));
    }

    @PostMapping("/google-login")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@RequestParam String idToken) throws Exception {
        AuthResponse data = authService.googleLogin(idToken);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Google login successful", data));
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK, "pong", "pong"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Logout successful", "Logged out"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal
    ) {
        authService.changePassword(principal.getName(), request);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Password updated successfully",
                "Password updated successfully"
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verify(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK, "Verification successful", "Verification successful"));
    }

    @GetMapping("/verify-link")
    public ResponseEntity<String> verifyLink(@RequestParam String email, @RequestParam String otp) {
        authService.verifyOtp(email, otp);
        return ResponseEntity.ok("Email verified successfully");
    }
    

    @GetMapping("/resend-verify-email")
    public ResponseEntity<ApiResponse<String>> resendVerifyEmail(@RequestParam String email) {
        authService.sendVerifyEmail(email);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Verification email resent", "Email resent"));
    }

    @GetMapping("/test-email")
    public ResponseEntity<ApiResponse<String>> testEmail() {
        authService.sendTestEmail();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Test email sent", "Email sent"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.sendResetPasswordOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "OTP sent to email if exists", "OTP sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Password reset successful", "Password reset"));
    }

}
