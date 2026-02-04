package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.UpdateVendorProfileRequest;
import fpt.project.NeoNHS.dto.request.VendorRegisterRequest;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.VendorProfile;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.service.VendorProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendorProfileServiceImpl implements VendorProfileService {

    private final VendorProfileRepository vendorProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public VendorProfileResponse createVendorAccount(VendorRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists in the system");
        }

        User user = User.builder()
                .fullname(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.VENDOR)
                .isActive(true)
                .isVerified(true)
                .isBanned(false)
                .build();

        User savedUser = userRepository.save(user);

        VendorProfile profile = VendorProfile.builder()
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .taxCode(request.getTaxCode())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankAccountName(request.getBankAccountName())
                .isVerified(true)
                .user(savedUser)
                .build();

        VendorProfile savedProfile = vendorProfileRepository.save(profile);

        return mapToVendorResponse(savedUser, savedProfile);
    }

    @Override
    public VendorProfileResponse getVendorProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        VendorProfile profile = user.getVendorProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("VendorProfile", "user_id", user.getId());
        }

        return mapToVendorResponse(user, profile);
    }

    @Override
    @Transactional
    public VendorProfileResponse updateVendorProfile(String email, UpdateVendorProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        VendorProfile profile = user.getVendorProfile();
        if (profile == null) {
            throw new BadRequestException("Vendor profile not initialized for this account");
        }

        if (request.getFullname() != null) user.setFullname(request.getFullname());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(user);

        if (request.getBusinessName() != null) profile.setBusinessName(request.getBusinessName());
        if (request.getDescription() != null) profile.setDescription(request.getDescription());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getLatitude() != null) profile.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) profile.setLongitude(request.getLongitude());
        if (request.getTaxCode() != null) profile.setTaxCode(request.getTaxCode());
        if (request.getBankName() != null) profile.setBankName(request.getBankName());
        if (request.getBankAccountNumber() != null) profile.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getBankAccountName() != null) profile.setBankAccountName(request.getBankAccountName());

        vendorProfileRepository.save(profile);

        return mapToVendorResponse(user, profile);
    }

    private VendorProfileResponse mapToVendorResponse(User user, VendorProfile vp) {
        return VendorProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .businessName(vp.getBusinessName())
                .description(vp.getDescription())
                .address(vp.getAddress())
                .latitude(vp.getLatitude())
                .longitude(vp.getLongitude())
                .taxCode(vp.getTaxCode())
                .bankName(vp.getBankName())
                .bankAccountNumber(vp.getBankAccountNumber())
                .bankAccountName(vp.getBankAccountName())
                .isVerifiedVendor(vp.getIsVerified())
                .build();
    }
}
