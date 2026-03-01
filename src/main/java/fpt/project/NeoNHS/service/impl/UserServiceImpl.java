package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.auth.UpdateUserProfileRequest;
import fpt.project.NeoNHS.dto.request.user.UserFilterRequest;
import fpt.project.NeoNHS.dto.response.auth.UserProfileResponse;
import fpt.project.NeoNHS.dto.response.user.UserResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.DuplicatePhonenumberException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.service.UserService;
import fpt.project.NeoNHS.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
                throw new DuplicatePhonenumberException("Phone number '" + request.getPhoneNumber() + "' is already in use!");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        userRepository.save(user);
        return mapToUserResponse(user);
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^0[0-9]{9}$");
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

    private UserResponse mapToAdminUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isActive(user.getIsActive())
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
                .build();
    }
}
