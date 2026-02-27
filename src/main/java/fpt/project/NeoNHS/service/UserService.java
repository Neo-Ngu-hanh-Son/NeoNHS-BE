package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.auth.UpdateUserProfileRequest;
import fpt.project.NeoNHS.dto.response.auth.UserProfileResponse;
import fpt.project.NeoNHS.dto.response.user.UserResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.UserRole;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    long countUsers();

    User createUser(User user);

    boolean existsByEmail(String email);

    Optional<User> findById(UUID id);
    UserProfileResponse getMyProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateUserProfileRequest request, UUID id);

    Page<UserResponse> getAllUsersWithPagination(
            int page, int size, String sortBy, String sortDir,
            String search, UserRole role, Boolean isBanned,
            Boolean deleted, Boolean includeDeleted);

    void toggleBanUser(UUID id, String reason);
}
