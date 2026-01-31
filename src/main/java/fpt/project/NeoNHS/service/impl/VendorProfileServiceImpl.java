package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.UpdateVendorProfileRequest;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.VendorProfile;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.service.VendorProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendorProfileServiceImpl implements VendorProfileService {

    private final VendorProfileRepository vendorProfileRepository;
    private final UserRepository userRepository;

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
        // 1. Tìm User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // 2. Cập nhật thông tin định danh (bảng users)
        user.setFullname(request.getFullname());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(user);

        // 3. Cập nhật thông tin kinh doanh (bảng vendor_profiles)
        VendorProfile profile = user.getVendorProfile();
        if (profile == null) {
            throw new BadRequestException("Vendor profile not initialized for this account");
        }

        profile.setBusinessName(request.getBusinessName());
        profile.setDescription(request.getDescription());
        profile.setAddress(request.getAddress());
        profile.setLatitude(request.getLatitude());
        profile.setLongitude(request.getLongitude());
        profile.setTaxCode(request.getTaxCode());
        profile.setBankName(request.getBankName());
        profile.setBankAccountNumber(request.getBankAccountNumber());
        profile.setBankAccountName(request.getBankAccountName());

        vendorProfileRepository.save(profile);

        return mapToVendorResponse(user, profile);
    }

    private VendorProfileResponse mapToVendorResponse(User user, VendorProfile vp) {
        // Sử dụng @SuperBuilder để kế thừa các trường từ UserProfileResponse
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
