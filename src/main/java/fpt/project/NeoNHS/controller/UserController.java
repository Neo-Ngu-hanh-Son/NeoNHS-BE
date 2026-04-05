package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.auth.UpdateUserProfileRequest;
import fpt.project.NeoNHS.dto.request.kyc.KycRequest;
import fpt.project.NeoNHS.dto.request.payout.CreatePayoutRequest;
import fpt.project.NeoNHS.dto.request.payout.WithdrawRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.auth.UserProfileResponse;
import fpt.project.NeoNHS.dto.response.kyc.KycResponse;
import fpt.project.NeoNHS.dto.response.payout.PayoutResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.service.FaceVerificationService;
import fpt.project.NeoNHS.service.UserService;
import fpt.project.NeoNHS.service.VnptEkycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.Normalizer;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PayoutController payoutController;
    private final FaceVerificationService faceVerificationService;
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

        int amount = withdrawRequest.getAmount();
        String livePhotoBase64 = withdrawRequest.getLivePhotoBase64();

        // 1. Load user từ JWT
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getName()));

        // 2. Validate KYC đã xác thực
        if (!Boolean.TRUE.equals(user.getKycVerified())) {
            throw new BadRequestException("You must complete KYC verification before withdrawing");
        }

        // 3. Face verification — so sánh ảnh live với embedding đã lưu từ KYC
        if (user.getFaceEmbedding() == null || user.getFaceEmbedding().isBlank()) {
            throw new BadRequestException("No face data found. Please redo KYC verification.");
        }
        if (livePhotoBase64 == null || livePhotoBase64.isBlank()) {
            throw new BadRequestException("Live photo is required for withdrawal face verification.");
        }

        try {
            boolean faceMatch = faceVerificationService.compareFaces(livePhotoBase64, user.getFaceEmbedding());
            if (!faceMatch) {
                log.warn("[Withdraw] Face verification FAILED for user: {}", user.getEmail());
                throw new BadRequestException(
                        "Face verification failed. The live photo does not match your KYC face. Please try again.");
            }
            log.info("[Withdraw] Face verification PASSED for user: {}", user.getEmail());
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Withdraw] Face verification error for user: {}. Error: {}", user.getEmail(), e.getMessage());
            throw new BadRequestException("Face verification service error: " + e.getMessage());
        }

        // 4. Validate thông tin ngân hàng
        if (user.getBankBin() == null || user.getBankBin().isBlank()) {
            throw new BadRequestException("Bank BIN code is not configured for this account");
        }
        if (user.getBankAccountNumber() == null || user.getBankAccountNumber().isBlank()) {
            throw new BadRequestException("Bank account number is not configured for this account");
        }
        if (user.getBankAccountName() == null || user.getBankAccountName().isBlank()) {
            throw new BadRequestException("Bank account name is not configured for this account");
        }

        // 5. So sánh bankAccountName với kycFullName (không dấu, uppercase)
        String normalizedBankName = removeDiacritics(user.getBankAccountName()).toUpperCase().trim();
        String normalizedKycName = removeDiacritics(user.getKycFullName()).toUpperCase().trim();

        if (!normalizedBankName.equals(normalizedKycName)) {
            log.warn("[Withdraw] Bank name mismatch: bankAccountName='{}' vs kycFullName='{}'",
                    normalizedBankName, normalizedKycName);
            throw new BadRequestException(String.format(
                    "Bank account name '%s' does not match KYC verified name '%s'. " +
                            "Please update your bank account information to match your KYC name.",
                    user.getBankAccountName(), user.getKycFullName()));
        }

        // 6. Validate balance
        double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
        if (currentBalance < amount) {
            throw new BadRequestException(String.format(
                    "Insufficient balance. Current: %.0f VND, Requested: %d VND",
                    currentBalance, amount));
        }

        // 7. Build CreatePayoutRequest từ thông tin user
        CreatePayoutRequest payoutRequest = CreatePayoutRequest.builder()
                .referenceId("withdraw_" + user.getId() + "_" + System.currentTimeMillis())
                .amount(amount)
                .description("Withdrawal for user ")
                .toBin(user.getBankBin())
                .toAccountNumber(user.getBankAccountNumber())
                .category(Collections.singletonList("withdrawal"))
                .build();

        log.info("[Withdraw] User {} requests {} VND → bank account {}",
                user.getEmail(), amount, user.getBankAccountNumber());

        // 8. Gọi PayoutController để chuyển khoản qua PayOS
        ResponseEntity<ApiResponse<PayoutResponse>> payoutResult = payoutController.createPayout(payoutRequest);

        // 9. Nếu PayOS trả về 2xx → trừ balance
        if (payoutResult.getStatusCode().is2xxSuccessful()) {
            user.setBalance(currentBalance - amount);
            userRepository.save(user);
            log.info("[Withdraw] Balance updated: {} → {} VND",
                    currentBalance, user.getBalance());
        }

        return payoutResult;
    }

    // =========================================================
    // Helpers
    // =========================================================

    private String removeDiacritics(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String result = input.replace('đ', 'd').replace('Đ', 'D');
        String normalized = Normalizer.normalize(result, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{M}");
        return pattern.matcher(normalized).replaceAll("");
    }
}
