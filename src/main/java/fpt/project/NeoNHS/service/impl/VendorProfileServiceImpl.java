package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.auth.UpdateVendorProfileRequest;
import fpt.project.NeoNHS.dto.request.auth.VendorRegisterRequest;
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

import java.util.UUID;

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
    public VendorProfileResponse updateVendorProfile(UUID id, String currentEmail, UpdateVendorProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new BadRequestException("Bạn không có quyền chỉnh sửa tài khoản này!");
        }

        VendorProfile profile = user.getVendorProfile();
        if (profile == null) {
            throw new BadRequestException("Hồ sơ Vendor chưa được khởi tạo");
        }

        if (request.getFullname() != null) user.setFullname(request.getFullname());

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BadRequestException("Số điện thoại này đã được sử dụng!");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        updateVendorFields(profile, request);

        userRepository.save(user);

        return mapToVendorResponse(user, profile);
    }

    // Hàm phụ để code sạch hơn
    private void updateVendorFields(VendorProfile profile, UpdateVendorProfileRequest req) {
        if (req.getBusinessName() != null) profile.setBusinessName(req.getBusinessName());
        if (req.getTaxCode() != null) profile.setTaxCode(req.getTaxCode());
        if (req.getBankName() != null) profile.setBankName(req.getBankName());
        if (req.getBankAccountNumber() != null) profile.setBankAccountNumber(req.getBankAccountNumber());
        if (req.getBankAccountName() != null) profile.setBankAccountName(req.getBankAccountName());
        if (req.getAddress() != null) profile.setAddress(req.getAddress());
        if (req.getLatitude() != null) profile.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) profile.setLongitude(req.getLongitude());
        if (req.getDescription() != null) profile.setDescription(req.getDescription());
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
