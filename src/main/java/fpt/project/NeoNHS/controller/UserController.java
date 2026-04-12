package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.auth.UpdateUserProfileRequest;
import fpt.project.NeoNHS.dto.request.kyc.KycRequest;
import fpt.project.NeoNHS.dto.request.payout.WithdrawRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.auth.UserProfileResponse;
import fpt.project.NeoNHS.dto.response.kyc.KycResponse;
import fpt.project.NeoNHS.dto.response.payout.PayoutResponse;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.service.UserService;
import fpt.project.NeoNHS.service.VnptEkycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final VnptEkycService vnptEkycService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Principal principal) {
        UserProfileResponse data = userService.getMyProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Profile retrieved successfully", data));
    }

    @PutMapping("/update-profile/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateBasicProfile(
            Principal principal, @PathVariable UUID id, @RequestBody UpdateUserProfileRequest request) {
        UserProfileResponse data = userService.updateProfile(principal.getName(), request, id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Basic info updated", data));
    }

    @PostMapping("/{id}/kyc")
    public ResponseEntity<ApiResponse<KycResponse>> performKyc(
            @PathVariable UUID id,
            @RequestBody KycRequest request) {

        log.info("KYC request for user: {}", id);
        KycResponse kycResponse = userService.performEkyc(id, request);

        if (kycResponse.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success(HttpStatus.OK, "KYC verification successful", kycResponse));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, kycResponse.getMessage()));
        }
    }

    @PostMapping("/check-liveness")
    public ResponseEntity<ApiResponse<Boolean>> checkLiveness(@RequestBody java.util.Map<String, String> body) {
        String base64Image = body.get("image");
        if (base64Image == null || base64Image.isEmpty()) {
            throw new BadRequestException("Image is required");
        }
        boolean isLive = vnptEkycService.checkLiveness(base64Image);
        if (isLive) {
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Liveness check passed", true));
        } else {
            return ResponseEntity.badRequest()
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST, "Spoofing detected or check failed"));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<PayoutResponse>> withdraw(
            Principal principal,
            @RequestBody WithdrawRequest withdrawRequest) {

        PayoutResponse response = userService.withdraw(principal.getName(), withdrawRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Withdrawal successful", response));
    }
}
