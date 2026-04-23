package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.auth.UpdateUserProfileRequest;
import fpt.project.NeoNHS.dto.request.kyc.KycRequest;
import fpt.project.NeoNHS.dto.response.admin.UserStatsResponse;
import fpt.project.NeoNHS.dto.response.auth.UserProfileResponse;
import fpt.project.NeoNHS.dto.response.kyc.KycResponse;
import fpt.project.NeoNHS.dto.request.user.UserFilterRequest;
import fpt.project.NeoNHS.dto.response.user.UserResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.DuplicatePhonenumberException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.dto.request.payout.CreatePayoutRequest;
import fpt.project.NeoNHS.dto.request.payout.WithdrawRequest;
import fpt.project.NeoNHS.dto.response.payout.PayoutResponse;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.service.UserService;
import fpt.project.NeoNHS.service.FaceVerificationService;
import fpt.project.NeoNHS.service.VnptEkycService;
import fpt.project.NeoNHS.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import fpt.project.NeoNHS.specification.UserSpecification;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VnptEkycService vnptEkycService;
    private final FaceVerificationService faceVerificationService;
    private final PayoutService payoutService;

    /**
     * Ngưỡng faceMatchScore tối thiểu để xác nhận KYC (85%)
     */
    private static final double FACE_MATCH_THRESHOLD = 85.0;

    @Override
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public UserProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String currentEmail, UpdateUserProfileRequest request, UUID id) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email '" + request.getEmail() + "' is already taken!");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            if (!isValidPhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            var existingUser = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElse(null);
            // If the phone number belongs to another user
            if (existingUser != null && !existingUser.getId().equals(id)) {
                throw new DuplicatePhonenumberException(
                        "Phone number '" + request.getPhoneNumber() + "' is already in use!");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        // Bank / payout info — chỉ update khi client gửi lên (non-null)
        if (request.getBankName() != null) {
            user.setBankName(request.getBankName());
        }
        if (request.getBankBin() != null) {
            user.setBankBin(request.getBankBin());
        }
        if (request.getBankAccountNumber() != null) {
            user.setBankAccountNumber(request.getBankAccountNumber());
        }
        if (request.getBankAccountName() != null) {
            user.setBankAccountName(request.getBankAccountName());
        }

        userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public KycResponse performEkyc(UUID userId, KycRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        if (Boolean.TRUE.equals(user.getKycVerified())) {
            throw new BadRequestException("User already completed KYC verification");
        }

        log.info("Starting eKYC for user: {}", userId);
        KycResponse kycResponse = vnptEkycService.performKyc(request);

        if (kycResponse.isSuccess()) {
            Double faceScore = kycResponse.getFaceMatchScore();

            if (faceScore != null && faceScore >= FACE_MATCH_THRESHOLD) {
                user.setKycVerified(true);

                String rawName = kycResponse.getFullName();
                String nameWithoutDiacritics = removeDiacritics(rawName);
                user.setKycFullName(nameWithoutDiacritics);

                user.setKycIdNumber(kycResponse.getIdNumber());

                // Extract face embedding from selfie and store for future face verification
                try {
                    String embedding = faceVerificationService.extractEmbedding(request.getSelfieImageBase64());
                    user.setFaceEmbedding(embedding);
                    log.info("Face embedding extracted and saved for user: {}", userId);
                } catch (Exception e) {
                    log.warn("Failed to extract face embedding for user: {}. " +
                            "KYC still succeeds but face verification for withdrawal won't work. Error: {}",
                            userId, e.getMessage());
                }

                userRepository.save(user);

                log.info("KYC successful for user: {}, name: {} (no diacritics: {}), faceScore: {}%",
                        userId, rawName, nameWithoutDiacritics, faceScore);

                kycResponse.setFullName(nameWithoutDiacritics);
                kycResponse.setMessage("KYC verification successful. Face match score: " + faceScore + "%");
            } else {
                kycResponse.setSuccess(false);
                kycResponse.setMessage(String.format(
                        "KYC verification failed. Face match score: %.2f%% (minimum required: %.0f%%)",
                        faceScore != null ? faceScore : 0.0, FACE_MATCH_THRESHOLD));
                log.warn("KYC failed for user: {}, faceScore: {}% (threshold: {}%)",
                        userId, faceScore, FACE_MATCH_THRESHOLD);
            }
        }

        return kycResponse;
    }

    @Override
    @Transactional
    public PayoutResponse withdraw(String email, WithdrawRequest request) {
        int amount = request.getAmount();
        String livePhotoBase64 = request.getLivePhotoBase64();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!Boolean.TRUE.equals(user.getKycVerified())) {
            throw new BadRequestException("You must complete KYC verification before withdrawing");
        }

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

        if (user.getBankBin() == null || user.getBankBin().isBlank()) {
            throw new BadRequestException("Bank BIN code is not configured for this account");
        }
        if (user.getBankAccountNumber() == null || user.getBankAccountNumber().isBlank()) {
            throw new BadRequestException("Bank account number is not configured for this account");
        }
        if (user.getBankAccountName() == null || user.getBankAccountName().isBlank()) {
            throw new BadRequestException("Bank account name is not configured for this account");
        }

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

        double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
        if (currentBalance < amount) {
            throw new BadRequestException(String.format(
                    "Insufficient balance. Current: %.0f VND, Requested: %d VND",
                    currentBalance, amount));
        }

        CreatePayoutRequest payoutRequest = CreatePayoutRequest.builder()
                .referenceId("withdraw_" + user.getId() + "_" + System.currentTimeMillis())
                .amount(amount)
                .description("Rut tien VND")
                .toBin(user.getBankBin())
                .toAccountNumber(user.getBankAccountNumber())
                .category(Collections.singletonList("withdrawal"))
                .build();

        log.info("[Withdraw] User {} requests {} VND → bank account {}",
                user.getEmail(), amount, user.getBankAccountNumber());

        PayoutResponse payoutResponse = payoutService.createPayout(payoutRequest);

        user.setBalance(currentBalance - amount);
        userRepository.save(user);
        log.info("[Withdraw] Balance updated: {} → {} VND", currentBalance, user.getBalance());

        return payoutResponse;
    }

    // =========================================================
    // Helpers
    // =========================================================

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^0[0-9]{9}$");
    }

    private String removeDiacritics(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input.replace('đ', 'd').replace('Đ', 'D');

        String normalized = Normalizer.normalize(result, Normalizer.Form.NFD);

        Pattern pattern = Pattern.compile("\\p{M}");
        return pattern.matcher(normalized).replaceAll("");
    }

    @Override
    @Transactional
    public void toggleBanUser(UUID id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        boolean newBanStatus = !user.getIsBanned();
        user.setIsBanned(newBanStatus);

        if (newBanStatus) {
            if (reason == null || reason.isBlank()) {
                throw new BadRequestException("Reason is required when banning a user");
            }
            user.setBanReason(reason);
            user.setBannedAt(java.time.LocalDateTime.now());
        } else {
            user.setBanReason(null);
            user.setBannedAt(null);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsersWithPagination(int page, int size, String sortBy,
            String sortDir, String search,
            UserRole role, Boolean isBanned,
            Boolean deleted, Boolean includeDeleted) {

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        int actualSize = Math.min(size, PaginationConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, actualSize, sort);

        UserFilterRequest filters = UserFilterRequest.builder()
                .search(search)
                .role(role)
                .isBanned(isBanned)
                .deleted(deleted)
                .includeDeleted(includeDeleted)
                .build();

        return userRepository.findAll(UserSpecification.withFilters(filters), pageable)
                .map(this::mapToAdminUserResponse);
    }

    @Override
    public UserStatsResponse getUserStats() {
        return UserStatsResponse.builder()
                .total(userRepository.count())
                .active(userRepository.countByIsActiveTrueAndIsBannedFalse())
                .banned(userRepository.countByIsBannedTrue())
                .unverified(userRepository.countByIsVerifiedFalse())
                .inactive(userRepository.countByIsActiveFalseAndIsBannedFalse(UserRole.TOURIST))
                .build();
    }

    private UserResponse mapToAdminUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .isBanned(user.getIsBanned())
                .banReason(user.getBanReason())
                .bannedAt(user.getBannedAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserProfileResponse mapToUserResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .bankName(user.getBankName())
                .bankBin(user.getBankBin())
                .bankAccountNumber(user.getBankAccountNumber())
                .bankAccountName(user.getBankAccountName())
                .balance(user.getBalance())
                .kycVerified(user.getKycVerified())
                .kycFullName(user.getKycFullName())
                .kycIdNumber(user.getKycIdNumber())
                .userPoint(user.getRewardPoints())
                .build();
    }
}
