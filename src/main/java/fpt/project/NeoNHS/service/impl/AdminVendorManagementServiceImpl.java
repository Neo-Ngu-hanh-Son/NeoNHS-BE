package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.admin.BanVendorRequest;
import fpt.project.NeoNHS.dto.request.admin.CreateVendorByAdminRequest;
import fpt.project.NeoNHS.dto.request.admin.UpdateVendorByAdminRequest;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.VendorProfile;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.service.AdminVendorManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminVendorManagementServiceImpl implements AdminVendorManagementService {

    private final VendorProfileRepository vendorProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public VendorProfileResponse createVendorByAdmin(CreateVendorByAdminRequest request) {
        log.info("Admin creating vendor account for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists in the system");
        }

        // Check if phone number already exists
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BadRequestException("Phone number already exists in the system");
            }
        }

        // Create User
        User user = User.builder()
                .fullname(request.getFullname())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.VENDOR)
                .isActive(true)
                .isVerified(true)
                .isBanned(false)
                .build();

        User savedUser = userRepository.save(user);

        // Create VendorProfile
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
                .isVerified(request.getIsVerified() != null ? request.getIsVerified() : true)
                .user(savedUser)
                .build();

        VendorProfile savedProfile = vendorProfileRepository.save(profile);

        log.info("Vendor account created successfully for email: {}", request.getEmail());
        return mapToVendorResponse(savedUser, savedProfile);
    }

    @Override
    public Page<VendorProfileResponse> getAllVendors(Pageable pageable) {
        log.info("Admin fetching all vendors, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<VendorProfile> vendorProfiles = vendorProfileRepository.findAll(pageable);
        return vendorProfiles.map(vp -> mapToVendorResponse(vp.getUser(), vp));
    }

    @Override
    public VendorProfileResponse getVendorById(UUID id) {
        log.info("Admin fetching vendor by ID: {}", id);
        VendorProfile profile = vendorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", id));
        return mapToVendorResponse(profile.getUser(), profile);
    }

    @Override
    @Transactional
    public VendorProfileResponse updateVendorByAdmin(UUID id, UpdateVendorByAdminRequest request) {
        log.info("Admin updating vendor profile with ID: {}", id);

        VendorProfile profile = vendorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", id));

        User user = profile.getUser();

        // Update User fields
        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BadRequestException("Phone number already exists in the system");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        // Update VendorProfile fields
        if (request.getBusinessName() != null) {
            profile.setBusinessName(request.getBusinessName());
        }

        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
        }

        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }

        if (request.getLatitude() != null) {
            profile.setLatitude(request.getLatitude());
        }

        if (request.getLongitude() != null) {
            profile.setLongitude(request.getLongitude());
        }

        if (request.getTaxCode() != null) {
            profile.setTaxCode(request.getTaxCode());
        }

        if (request.getBankName() != null) {
            profile.setBankName(request.getBankName());
        }

        if (request.getBankAccountNumber() != null) {
            profile.setBankAccountNumber(request.getBankAccountNumber());
        }

        if (request.getBankAccountName() != null) {
            profile.setBankAccountName(request.getBankAccountName());
        }

        if (request.getIsVerified() != null) {
            profile.setIsVerified(request.getIsVerified());
        }

        userRepository.save(user);
        vendorProfileRepository.save(profile);

        log.info("Vendor profile updated successfully for ID: {}", id);
        return mapToVendorResponse(user, profile);
    }

    @Override
    @Transactional
    public VendorProfileResponse banVendor(UUID id, BanVendorRequest request) {
        log.info("Admin banning vendor with ID: {}", id);

        VendorProfile profile = vendorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", id));

        User user = profile.getUser();

        if (user.getIsBanned()) {
            throw new BadRequestException("Vendor is already banned");
        }

        user.setIsBanned(true);
        user.setIsActive(false);
        userRepository.save(user);

        log.info("Vendor banned successfully: {} - Reason: {}", user.getEmail(), request.getReason());
        return mapToVendorResponse(user, profile);
    }

    @Override
    @Transactional
    public VendorProfileResponse unbanVendor(UUID id) {
        log.info("Admin unbanning vendor with ID: {}", id);

        VendorProfile profile = vendorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", id));

        User user = profile.getUser();

        if (!user.getIsBanned()) {
            throw new BadRequestException("Vendor is not banned");
        }

        user.setIsBanned(false);
        user.setIsActive(true);
        userRepository.save(user);

        log.info("Vendor unbanned successfully: {}", user.getEmail());
        return mapToVendorResponse(user, profile);
    }

    @Override
    @Transactional
    public void deleteVendor(UUID id) {
        log.info("Admin deleting vendor with ID: {}", id);

        VendorProfile profile = vendorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", id));

        User user = profile.getUser();

        // Soft delete: deactivate the account
        user.setIsActive(false);
        user.setIsBanned(true);
        userRepository.save(user);

        log.info("Vendor deleted (soft delete) successfully: {}", user.getEmail());
    }

    @Override
    public Page<VendorProfileResponse> searchVendors(String keyword, Pageable pageable) {
        log.info("Admin searching vendors with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVendors(pageable);
        }

        Page<VendorProfile> vendorProfiles = vendorProfileRepository.searchByKeyword(keyword.trim(), pageable);
        return vendorProfiles.map(vp -> mapToVendorResponse(vp.getUser(), vp));
    }

    @Override
    public Page<VendorProfileResponse> filterVendorsByVerification(Boolean isVerified, Pageable pageable) {
        log.info("Admin filtering vendors by verification status: {}", isVerified);

        Page<VendorProfile> vendorProfiles = vendorProfileRepository.findByIsVerified(isVerified, pageable);
        return vendorProfiles.map(vp -> mapToVendorResponse(vp.getUser(), vp));
    }

    @Override
    public Page<VendorProfileResponse> filterVendorsByBannedStatus(Boolean isBanned, Pageable pageable) {
        log.info("Admin filtering vendors by banned status: {}", isBanned);

        Page<VendorProfile> vendorProfiles = vendorProfileRepository.findByUserIsBanned(isBanned, pageable);
        return vendorProfiles.map(vp -> mapToVendorResponse(vp.getUser(), vp));
    }

    @Override
    public Page<VendorProfileResponse> filterVendorsByActiveStatus(Boolean isActive, Pageable pageable) {
        log.info("Admin filtering vendors by active status: {}", isActive);

        Page<VendorProfile> vendorProfiles = vendorProfileRepository.findByUserIsActive(isActive, pageable);
        return vendorProfiles.map(vp -> mapToVendorResponse(vp.getUser(), vp));
    }

    @Override
    public Page<VendorProfileResponse> advancedSearchAndFilter(
            String keyword,
            Boolean isVerified,
            Boolean isBanned,
            Boolean isActive,
            Pageable pageable) {

        log.info("Admin performing advanced search and filter - keyword: {}, isVerified: {}, isBanned: {}, isActive: {}",
                keyword, isVerified, isBanned, isActive);

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<VendorProfile> vendorProfiles = vendorProfileRepository.advancedSearchAndFilter(
                searchKeyword, isVerified, isBanned, isActive, pageable
        );

        return vendorProfiles.map(vp -> mapToVendorResponse(vp.getUser(), vp));
    }

    // Mapper method
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
                .isActive(user.getIsActive())
                .isBanned(user.getIsBanned())
                .build();
    }
}
