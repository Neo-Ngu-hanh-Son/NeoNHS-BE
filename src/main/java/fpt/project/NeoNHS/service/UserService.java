package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.auth.UpdateUserProfileRequest;
import fpt.project.NeoNHS.dto.request.kyc.KycRequest;
import fpt.project.NeoNHS.dto.response.admin.UserStatsResponse;
import fpt.project.NeoNHS.dto.response.auth.UserProfileResponse;
import fpt.project.NeoNHS.dto.response.kyc.KycResponse;
import fpt.project.NeoNHS.dto.request.payout.WithdrawRequest;
import fpt.project.NeoNHS.dto.response.payout.PayoutResponse;
import fpt.project.NeoNHS.dto.response.user.UserResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.UserRole;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserStatsResponse getUserStats();

    long countUsers();

    User createUser(User user);

    boolean existsByEmail(String email);

    Optional<User> findById(UUID id);

    UserProfileResponse getMyProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateUserProfileRequest request, UUID id);

    KycResponse performEkyc(UUID userId, KycRequest request);

    PayoutResponse withdraw(String email, WithdrawRequest request);

    Page<UserResponse> getAllUsersWithPagination(
            int page, int size, String sortBy, String sortDir,
            String search, UserRole role, Boolean isBanned,
            Boolean deleted, Boolean includeDeleted);

    void toggleBanUser(UUID id, String reason);
}
