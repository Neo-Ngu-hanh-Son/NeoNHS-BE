package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.EmailTemplate;
import fpt.project.NeoNHS.dto.request.admin.BanVendorRequest;
import fpt.project.NeoNHS.dto.request.admin.CreateVendorByAdminRequest;
import fpt.project.NeoNHS.dto.request.admin.UpdateVendorByAdminRequest;
import fpt.project.NeoNHS.dto.response.admin.VendorManagementStatsResponse;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.VendorProfile;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.repository.WorkshopSessionRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.repository.projection.VendorCountProjection;
import fpt.project.NeoNHS.service.AdminVendorManagementService;
import fpt.project.NeoNHS.service.MailService;
import fpt.project.NeoNHS.service.RedisAuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminVendorManagementServiceImpl implements AdminVendorManagementService {

    private final VendorProfileRepository vendorProfileRepository;
    private final UserRepository userRepository;
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final RedisAuthService redisAuthService;

    @Value("${app.be-url-setpassword}")
    private String feUrl;

    @Override
    @Transactional
    public VendorProfileResponse createVendorByAdmin(CreateVendorByAdminRequest request) {
        log.info("Admin creating vendor account for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists in the system");
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BadRequestException("Phone number already exists in the system");
            }
        }

        // Tạo User
        User user = User.builder()
                .fullname(request.getFullname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.VENDOR)
                .isBanned(false)
                .isActive(false)
                .isVerified(false)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();

        User savedUser = userRepository.save(user);

        // Tạo VendorProfile
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
                .isVerified(request.getIsVerified() != null ? request.getIsVerified() : false)
                .user(savedUser)
                .build();

        vendorProfileRepository.save(profile);

        // Xử lý Token và gửi Email
        String token = UUID.randomUUID().toString();
        redisAuthService.saveSetPasswordToken(user.getEmail(), token);

        mailService.sendSetPasswordEmailAsync(
                savedUser,
                EmailTemplate.SET_PASSWORD,
                token,
                feUrl);

        log.info("Vendor account created and set-password email sent to: {}", request.getEmail());
        return mapToVendorResponse(savedUser, profile);
    }

    @Override
    public Page<VendorProfileResponse> getAllVendors(Pageable pageable) {
        return listVendors(null, null, null, null, pageable);
    }

    @Override
    public Page<VendorProfileResponse> listVendors(
            String keyword,
            Boolean isActive,
            Boolean isBanned,
            Boolean isVerified,
            Pageable pageable) {
        log.info(
                "Admin listing vendors - keyword: {}, isActive: {}, isBanned: {}, isVerified: {}, page: {}, size: {}",
                keyword, isActive, isBanned, isVerified, pageable.getPageNumber(), pageable.getPageSize());

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<VendorProfile> vendorProfiles = vendorProfileRepository.advancedSearchAndFilter(
                searchKeyword, isVerified, isBanned, isActive, pageable);

        List<VendorProfile> content = vendorProfiles.getContent();
        if (content.isEmpty()) {
            return vendorProfiles.map(vp -> mapToVendorResponse(vp.getUser(), vp));
        }

        List<UUID> vendorIds = content.stream().map(VendorProfile::getId).toList();

        Map<UUID, Long> totalTemplatesByVendorId = toCountMap(
                workshopTemplateRepository.countTemplatesByVendorIds(vendorIds));
        Map<UUID, Long> activeTemplatesByVendorId = toCountMap(
                workshopTemplateRepository.countTemplatesByVendorIdsAndStatus(vendorIds, WorkshopStatus.ACTIVE));
        Map<UUID, Long> totalSessionsByVendorId = toCountMap(
                workshopSessionRepository.countSessionsByVendorIds(vendorIds));

        List<VendorProfileResponse> responses = content.stream()
                .map(vp -> mapToVendorResponseWithStats(
                        vp.getUser(),
                        vp,
                        totalTemplatesByVendorId.getOrDefault(vp.getId(), 0L),
                        activeTemplatesByVendorId.getOrDefault(vp.getId(), 0L),
                        totalSessionsByVendorId.getOrDefault(vp.getId(), 0L)))
                .toList();

        return new PageImpl<>(responses, pageable, vendorProfiles.getTotalElements());
    }

    @Override
    public VendorProfileResponse getVendorById(UUID id) {
        log.info("Admin fetching vendor by ID: {}", id);
        VendorProfile profile = vendorProfileRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "user_id", id));
        return mapToVendorResponse(profile.getUser(), profile);
    }

    @Override
    @Transactional
    public VendorProfileResponse updateVendorByAdmin(UUID id, UpdateVendorByAdminRequest request) {
        log.info("Admin updating vendor profile with ID: {}", id);

        VendorProfile profile = vendorProfileRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "user_id", id));

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

        VendorProfile profile = vendorProfileRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "user_id", id));

        User user = profile.getUser();

        if (user.getIsBanned()) {
            throw new BadRequestException("Vendor is already banned");
        }

        user.setIsBanned(true);
        user.setIsActive(false);
        user.setBanReason(request.getReason());
        user.setBannedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Vendor banned successfully: {} - Reason: {}", user.getEmail(), request.getReason());
        return mapToVendorResponse(user, profile);
    }

    @Override
    @Transactional
    public VendorProfileResponse unbanVendor(UUID id) {
        log.info("Admin unbanning vendor with ID: {}", id);

        VendorProfile profile = vendorProfileRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "user_id", id));

        User user = profile.getUser();

        if (!user.getIsBanned()) {
            throw new BadRequestException("Vendor is not banned");
        }

        user.setIsBanned(false);
        user.setIsActive(true);
        user.setBanReason(null);
        user.setBannedAt(null);
        userRepository.save(user);

        log.info("Vendor unbanned successfully: {}", user.getEmail());
        return mapToVendorResponse(user, profile);
    }

    @Override
    @Transactional
    public VendorProfileResponse verifyVendor(UUID id) {
        log.info("Admin verifying vendor with ID: {}", id);

        VendorProfile profile = vendorProfileRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "user_id", id));

        if (Boolean.TRUE.equals(profile.getIsVerified())) {
            throw new BadRequestException("Vendor is already verified");
        }

        profile.setIsVerified(true);
        vendorProfileRepository.save(profile);

        log.info("Vendor verified successfully: {}", profile.getUser().getEmail());
        return mapToVendorResponse(profile.getUser(), profile);
    }

    @Override
    @Transactional
    public void deleteVendor(UUID id) {
        log.info("Admin deleting vendor with ID: {}", id);

        VendorProfile profile = vendorProfileRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "user_id", id));

        User user = profile.getUser();

        // Soft delete: deactivate the account
        user.setIsActive(false);
        user.setIsBanned(true);
        userRepository.save(user);

        log.info("Vendor deleted (soft delete) successfully: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void hardDeleteVendor(UUID id) {
        log.info("Admin hard deleting vendor with ID: {}", id);

        VendorProfile profile = vendorProfileRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "user_id", id));

        User user = profile.getUser();

        // VendorProfile has cascade delete from User? Let's assume we delete manually
        // if not.
        // The entity showed: @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,
        // fetch = FetchType.LAZY)
        // so deleting user should delete profile.
        userRepository.delete(user);

        log.info("Vendor hard deleted successfully: {}", user.getEmail());
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

        log.info(
                "Admin performing advanced search and filter - keyword: {}, isVerified: {}, isBanned: {}, isActive: {}",
                keyword, isVerified, isBanned, isActive);

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<VendorProfile> vendorProfiles = vendorProfileRepository.advancedSearchAndFilter(
                searchKeyword, isVerified, isBanned, isActive, pageable);

        return vendorProfiles.map(vp -> mapToVendorResponse(vp.getUser(), vp));
    }

    @Override
    public VendorManagementStatsResponse getVendorManagementStats() {
        long total = vendorProfileRepository.count();
        long active = vendorProfileRepository.countByUserIsActiveTrue();
        long verified = vendorProfileRepository.countByIsVerifiedTrue();
        long banned = vendorProfileRepository.countByUserIsBannedTrue();
        long pending = vendorProfileRepository.countByIsVerifiedFalseAndUserIsActiveTrueAndUserIsBannedFalse();

        return VendorManagementStatsResponse.builder()
                .total(total)
                .active(active)
                .verified(verified)
                .banned(banned)
                .pendingVerification(pending)
                .build();
    }

    // Mapper method
    private VendorProfileResponse mapToVendorResponse(User user, VendorProfile vp) {
        return VendorProfileResponse.builder()
                .id(user.getId())
                .userId(vp.getId())
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
                .createdAt(user.getCreatedAt())
                .build();
    }

    private VendorProfileResponse mapToVendorResponseWithStats(
            User user,
            VendorProfile vp,
            long totalTemplates,
            long activeTemplates,
            long totalSessions) {
        return VendorProfileResponse.builder()
                .id(user.getId())
                .userId(vp.getId())
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
                .createdAt(user.getCreatedAt())
                .totalTemplates(totalTemplates)
                .activeTemplates(activeTemplates)
                .totalSessions(totalSessions)
                .build();
    }

    private Map<UUID, Long> toCountMap(List<VendorCountProjection> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }

        return rows.stream()
                .filter(r -> r.getVendorId() != null && r.getCount() != null)
                .collect(Collectors.toMap(
                        VendorCountProjection::getVendorId,
                        VendorCountProjection::getCount,
                        Long::sum));
    }
}