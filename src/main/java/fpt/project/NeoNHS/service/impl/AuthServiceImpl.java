package fpt.project.NeoNHS.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import fpt.project.NeoNHS.constants.EmailTemplate;
import fpt.project.NeoNHS.dto.request.auth.LoginRequest;
import fpt.project.NeoNHS.dto.request.auth.RegisterRequest;
import fpt.project.NeoNHS.dto.response.AuthResponse;
import fpt.project.NeoNHS.dto.response.auth.UserInfoResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.RequestGoogleAccountException;
import fpt.project.NeoNHS.helpers.GoogleTokenVerifier;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.security.JwtTokenProvider;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.AuthService;
import fpt.project.NeoNHS.service.MailService;
import fpt.project.NeoNHS.service.RedisAuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final MailService mailService;
    private final RedisAuthService redisAuthService;

    @Value("${app.fe-url}")
    private String appUrl;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow();

        String googleAuthDefaultPassword = passwordEncoder.encode("google_oauth2_user");
        if (user.getPasswordHash().equals(googleAuthDefaultPassword)) {
            throw new RequestGoogleAccountException();
        }

        if (!user.getIsActive()) {
            throw new BadRequestException("not activated");
        }

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userInfo(UserInfoResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullname(user.getFullname())
                        .role(user.getRole())
                        .avatarUrl(user.getAvatarUrl())
                        .isActive(user.getIsActive())
                        .isVerified(user.getIsVerified())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .isBanned(user.getIsBanned())
                        .updatedAt(user.getUpdatedAt())
                        .phoneNumber(user.getPhoneNumber())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .fullname(request.getFullname())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.TOURIST)
                .isActive(false)
                .build();

        userRepository.save(user);

        String verifyToken = generateVerifyToken();
        redisAuthService.saveOtp(user.getEmail(), verifyToken);
        mailService.sendVerifyEmailAsync(user, EmailTemplate.VERIFY_ACCOUNT, verifyToken, appUrl);
        return AuthResponse.builder()
                .accessToken(null)
                .tokenType("Bearer")
                .userInfo(null)
                .build();
    }

    @Override
    public void sendVerifyEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        String verifyToken = generateVerifyToken();
        redisAuthService.saveOtp(user.getEmail(), verifyToken);
        mailService.sendVerifyEmailAsync(user, EmailTemplate.VERIFY_ACCOUNT, verifyToken, appUrl);
    }

    private String generateVerifyToken() {
        return String.valueOf(new Random().nextInt(100000, 999999));
    }

    @Override
    public AuthResponse googleLogin(String idToken) {
        try {
            GoogleIdToken.Payload result = googleTokenVerifier.verify(idToken);
            System.out.println("[AuthServiceImpl] Google ID Token payload email: " + result.getEmail());
            User user = userRepository.findByEmail(result.getEmail())
                    .orElseGet(() -> createUserFromGooglePayload(result));
            // Generate JWT token
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            userPrincipal.getAuthorities()
                    );

            return getAuthResponse(user, authentication);
        } catch (Exception e) {
            System.out.println("[AuthServiceImpl] Google login failed: " + e.getMessage());
            throw new BadRequestException("Google login failed: " + e.getMessage());
        }
    }

    public void sendTestEmail() {
        User u = User.builder()
                .fullname("Test User")
                .email("phamminhkiet24@gmail.com")
                .role(UserRole.TOURIST)
                .avatarUrl("defaultAvatar.png")
                .passwordHash(passwordEncoder.encode("test_password"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        mailService.sendVerifyEmailAsync(u, EmailTemplate.VERIFY_ACCOUNT, generateVerifyToken(), appUrl);
    }

    public void verifyOtp(String email, String otp) {
        boolean res = redisAuthService.verifyOtp(email, otp);
        if (res) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadRequestException("User not found"));
            user.setIsActive(true);
            userRepository.save(user);
        } else {
            throw new BadRequestException("Invalid OTP");
        }
    }

    private AuthResponse getAuthResponse(User user, Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userInfo(UserInfoResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullname(user.getFullname())
                        .role(user.getRole())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }

    private User createUserFromGooglePayload(GoogleIdToken.Payload result) {
        User u = User.builder()
                .fullname((String) result.get("name"))
                .email(result.getEmail())
                .role(UserRole.TOURIST)
                .avatarUrl(result.get("picture") != null ? (String) result.get("picture") : "defaultAvatar.png")
                .passwordHash(passwordEncoder.encode("google_oauth2_user"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(u);
    }
}
