package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.auth.UpdateUserProfileRequest;
import fpt.project.NeoNHS.dto.request.kyc.KycRequest;
import fpt.project.NeoNHS.dto.response.auth.UserProfileResponse;
import fpt.project.NeoNHS.dto.response.kyc.KycResponse;
import fpt.project.NeoNHS.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    long countUsers();

    User createUser(User user);

    boolean existsByEmail(String email);

    Optional<User> findById(UUID id);

    UserProfileResponse getMyProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateUserProfileRequest request, UUID id);

    KycResponse performEkyc(UUID userId, KycRequest request);
}
