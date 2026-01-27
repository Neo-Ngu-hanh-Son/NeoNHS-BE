package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.auth.LoginRequest;
import fpt.project.NeoNHS.dto.request.auth.RegisterRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.AuthResponse;
import fpt.project.NeoNHS.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Login successful", data));
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
        System.out.println("Ping received");
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK, "pong", "pong"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Logout successful", "Logged out"));
    }

    @GetMapping("/test-email")
    public ResponseEntity<ApiResponse<String>> testEmail() {
        authService.sendTestEmail();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Test email sent", "Email sent"));
    }
}
