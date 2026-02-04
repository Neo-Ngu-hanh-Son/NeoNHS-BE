package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.UpdateUserProfileRequest;
import fpt.project.NeoNHS.dto.response.auth.UserProfileResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.DuplicatePhonenumberException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.service.UserService;
import lombok.RequiredArgsConstructor;
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

        user.setFullname(request.getFullname());
        user.setAvatarUrl(request.getAvatarUrl());

        userRepository.save(user);
        return mapToUserResponse(user);
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^0[0-9]{9}$");
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
