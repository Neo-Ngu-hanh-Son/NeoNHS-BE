package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.voucher.CreateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.UpdateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.VoucherFilterRequest;
import fpt.project.NeoNHS.dto.response.voucher.UserVoucherRespone;
import fpt.project.NeoNHS.dto.response.voucher.VoucherClassificationResult;
import fpt.project.NeoNHS.dto.response.voucher.VoucherResponse;
import fpt.project.NeoNHS.entity.*;
import fpt.project.NeoNHS.enums.*;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.repository.*;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.VoucherService;
import fpt.project.NeoNHS.specification.VoucherSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static fpt.project.NeoNHS.helpers.AuthHelper.getCurrentUserPrincipal;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final TicketCatalogRepository ticketCatalogRepository;

    // ==================== ADMIN ====================

    @Override
    @Transactional
    public VoucherResponse createAdminVoucher(CreateVoucherRequest request) {
        validateUniqueCode(request.getCode());
        validateVoucherTypeFields(request);
        validateDateRange(request.getStartDate(), request.getEndDate());

        User currentUser = getCurrentUser();
        Voucher voucher = buildVoucherFromRequest(request);
        voucher.setScope(VoucherScope.PLATFORM);
        voucher.setCreatedByUser(currentUser);

        // For FREE_SERVICE type, resolve the ticket catalog
        if (request.getVoucherType() == VoucherType.FREE_SERVICE && request.getFreeTicketCatalogId() != null) {
            TicketCatalog ticketCatalog = ticketCatalogRepository.findById(request.getFreeTicketCatalogId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket catalog not found"));
            voucher.setFreeTicketCatalog(ticketCatalog);
        }

        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getAllVouchers(VoucherFilterRequest filter, Pageable pageable) {
        var spec = VoucherSpecification.withFilters(filter);
        return voucherRepository.findAll(spec, pageable).map(VoucherResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(UUID id) {
        Voucher voucher = getActiveVoucher(id);
        return VoucherResponse.fromEntity(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(UUID id, UpdateVoucherRequest request) {
        Voucher voucher = getActiveVoucher(id);
        applyUpdates(voucher, request);
        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public void deleteVoucher(UUID id) {
        Voucher voucher = getActiveVoucher(id);
        UserPrincipal principal = getCurrentUserPrincipal();
        voucher.setDeletedAt(LocalDateTime.now());
        voucher.setDeletedBy(principal.getId());
        voucher.setStatus(VoucherStatus.INACTIVE);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void hardDeleteVoucher(UUID id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        if (userVoucherRepository.existsByVoucher_IdAndIsUsedTrue(id)) {
            throw new BadRequestException(
                    "Cannot permanently delete voucher that has been used in orders");
        }

        userVoucherRepository.deleteByVoucher_Id(id);
        voucherRepository.delete(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse restoreVoucher(UUID id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        if (voucher.getDeletedAt() == null) {
            throw new BadRequestException("Voucher is not deleted");
        }

        voucher.setDeletedAt(null);
        voucher.setDeletedBy(null);
        voucher.setStatus(VoucherStatus.ACTIVE);
        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    // ==================== VENDOR ====================

    @Override
    @Transactional
    public VoucherResponse createVendorVoucher(CreateVoucherRequest request) {
        validateUniqueCode(request.getCode());
        validateVoucherTypeFields(request);
        validateDateRange(request.getStartDate(), request.getEndDate());

        // Vendor cannot create BONUS_POINTS or FREE_SERVICE (platform-only types)
        if (request.getVoucherType() == VoucherType.BONUS_POINTS
                || request.getVoucherType() == VoucherType.FREE_SERVICE) {
            throw new BadRequestException("Vendors can only create DISCOUNT or GIFT_PRODUCT vouchers");
        }

        User currentUser = getCurrentUser();
        VendorProfile vendorProfile = vendorProfileRepository.findByUserEmail(currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        Voucher voucher = buildVoucherFromRequest(request);
        voucher.setScope(VoucherScope.VENDOR);
        voucher.setCreatedByUser(currentUser);
        voucher.setVendor(vendorProfile);

        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getMyVendorVouchers(VoucherFilterRequest filter, Pageable pageable) {
        User currentUser = getCurrentUser();
        VendorProfile vendorProfile = vendorProfileRepository.findByUserEmail(currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        var spec = VoucherSpecification.withVendorFilters(vendorProfile.getId(), filter);
        return voucherRepository.findAll(spec, pageable).map(VoucherResponse::fromEntity);
    }

    @Override
    @Transactional
    public VoucherResponse updateVendorVoucher(UUID id, UpdateVoucherRequest request) {
        Voucher voucher = getActiveVoucher(id);
        validateVendorOwnership(voucher);
        applyUpdates(voucher, request);
        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public void deleteVendorVoucher(UUID id) {
        Voucher voucher = getActiveVoucher(id);
        validateVendorOwnership(voucher);
        UserPrincipal principal = getCurrentUserPrincipal();
        voucher.setDeletedAt(LocalDateTime.now());
        voucher.setDeletedBy(principal.getId());
        voucher.setStatus(VoucherStatus.INACTIVE);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void hardDeleteVendorVoucher(UUID id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        validateVendorOwnership(voucher);

        if (userVoucherRepository.existsByVoucher_IdAndIsUsedTrue(id)) {
            throw new BadRequestException(
                    "Cannot permanently delete voucher that has been used in orders");
        }

        userVoucherRepository.deleteByVoucher_Id(id);
        voucherRepository.delete(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse restoreVendorVoucher(UUID id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        validateVendorOwnership(voucher);

        if (voucher.getDeletedAt() == null) {
            throw new BadRequestException("Voucher is not deleted");
        }

        voucher.setDeletedAt(null);
        voucher.setDeletedBy(null);
        voucher.setStatus(VoucherStatus.ACTIVE);
        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    // ==================== TOURIST ====================

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getAvailablePlatformVouchers(Pageable pageable) {
        return voucherRepository.findAvailableVouchers(
                        VoucherScope.PLATFORM, VoucherStatus.ACTIVE, LocalDateTime.now(), pageable)
                .map(VoucherResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getAvailableVendorVouchers(UUID vendorId, Pageable pageable) {
        return voucherRepository.findAvailableVouchersByVendor(
                        vendorId, VoucherStatus.ACTIVE, LocalDateTime.now(), pageable)
                .map(VoucherResponse::fromEntity);
    }

    @Override
    @Transactional
    public UserVoucherRespone collectVoucher(UUID voucherId) {
        User currentUser = getCurrentUser();
        Voucher voucher = getActiveVoucher(voucherId);

        // Validate voucher is still available
        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new BadRequestException("Voucher is not active");
        }
        if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Voucher has expired");
        }
        if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Voucher is not yet available");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsageCount() >= voucher.getUsageLimit()) {
            throw new BadRequestException("Voucher usage limit has been reached");
        }

        // Check if user already collected this voucher
        if (userVoucherRepository.existsByUser_IdAndVoucher_Id(currentUser.getId(), voucherId)) {
            throw new BadRequestException("You have already collected this voucher");
        }

        UserVoucher userVoucher = UserVoucher.builder()
                .user(currentUser)
                .voucher(voucher)
                .obtainedDate(LocalDateTime.now())
                .isUsed(false)
                .build();

        return UserVoucherRespone.fromEntity(userVoucherRepository.save(userVoucher));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserVoucherRespone> getMyVouchers(Boolean isUsed, Pageable pageable) {
        UserPrincipal principal = getCurrentUserPrincipal();
        Page<UserVoucher> page;
        if (isUsed != null) {
            page = userVoucherRepository.findByUser_IdAndIsUsed(principal.getId(), isUsed, pageable);
        } else {
            page = userVoucherRepository.findByUser_Id(principal.getId(), pageable);
        }
        return page.map(UserVoucherRespone::fromEntity);
    }

    // ==================== PRIVATE HELPERS ====================

    private Voucher buildVoucherFromRequest(CreateVoucherRequest request) {
        return Voucher.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .voucherType(request.getVoucherType())
                .applicableProduct(request.getApplicableProduct())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountValue(request.getMaxDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .giftDescription(request.getGiftDescription())
                .giftImageUrl(request.getGiftImageUrl())
                .bonusPointsValue(request.getBonusPointsValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .maxUsagePerUser(request.getMaxUsagePerUser())
                .status(VoucherStatus.ACTIVE)
                .build();
    }

    private void applyUpdates(Voucher voucher, UpdateVoucherRequest request) {
        if (request.getDescription() != null) voucher.setDescription(request.getDescription());
        if (request.getApplicableProduct() != null) voucher.setApplicableProduct(request.getApplicableProduct());
        if (request.getDiscountType() != null) voucher.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null) voucher.setDiscountValue(request.getDiscountValue());
        if (request.getMaxDiscountValue() != null) voucher.setMaxDiscountValue(request.getMaxDiscountValue());
        if (request.getMinOrderValue() != null) voucher.setMinOrderValue(request.getMinOrderValue());
        if (request.getGiftDescription() != null) voucher.setGiftDescription(request.getGiftDescription());
        if (request.getGiftImageUrl() != null) voucher.setGiftImageUrl(request.getGiftImageUrl());
        if (request.getBonusPointsValue() != null) voucher.setBonusPointsValue(request.getBonusPointsValue());
        if (request.getStartDate() != null) voucher.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) voucher.setEndDate(request.getEndDate());
        if (request.getUsageLimit() != null) voucher.setUsageLimit(request.getUsageLimit());
        if (request.getMaxUsagePerUser() != null) voucher.setMaxUsagePerUser(request.getMaxUsagePerUser());
        if (request.getStatus() != null) voucher.setStatus(request.getStatus());

        if (request.getFreeTicketCatalogId() != null) {
            TicketCatalog tc = ticketCatalogRepository.findById(request.getFreeTicketCatalogId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket catalog not found"));
            voucher.setFreeTicketCatalog(tc);
        }

        validateDateRange(voucher.getStartDate(), voucher.getEndDate());
        validateDiscountPercent(voucher);
    }

    private void validateUniqueCode(String code) {
        if (voucherRepository.existsByCode(code.toUpperCase())) {
            throw new BadRequestException("Voucher code '" + code + "' already exists");
        }
    }

    private void validateVoucherTypeFields(CreateVoucherRequest request) {
        switch (request.getVoucherType()) {
            case DISCOUNT -> {
                if (request.getDiscountType() == null) {
                    throw new BadRequestException("Discount type is required for DISCOUNT voucher");
                }
                if (request.getDiscountValue() == null) {
                    throw new BadRequestException("Discount value is required for DISCOUNT voucher");
                }
                if (request.getDiscountType() == DiscountType.PERCENT
                        && request.getDiscountValue().compareTo(new java.math.BigDecimal("100")) > 0) {
                    throw new BadRequestException("Percentage discount value must not exceed 100");
                }
            }
            case GIFT_PRODUCT -> {
                if (request.getGiftDescription() == null || request.getGiftDescription().isBlank()) {
                    throw new BadRequestException("Gift description is required for GIFT_PRODUCT voucher");
                }
            }
            case BONUS_POINTS -> {
                if (request.getBonusPointsValue() == null) {
                    throw new BadRequestException("Bonus points value is required for BONUS_POINTS voucher");
                }
            }
            case FREE_SERVICE -> {
                if (request.getFreeTicketCatalogId() == null) {
                    throw new BadRequestException("Free ticket catalog ID is required for FREE_SERVICE voucher");
                }
            }
        }
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new BadRequestException("End date must be after start date");
        }
    }

    private void validateDiscountPercent(Voucher voucher) {
        if (voucher.getVoucherType() == VoucherType.DISCOUNT
                && voucher.getDiscountType() == DiscountType.PERCENT
                && voucher.getDiscountValue() != null
                && voucher.getDiscountValue().compareTo(new java.math.BigDecimal("100")) > 0) {
            throw new BadRequestException("Percentage discount value must not exceed 100");
        }
    }

    private Voucher getActiveVoucher(UUID id) {
        return voucherRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
    }

    private void validateVendorOwnership(Voucher voucher) {
        User currentUser = getCurrentUser();
        VendorProfile vendorProfile = vendorProfileRepository.findByUserEmail(currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        if (voucher.getVendor() == null || !voucher.getVendor().getId().equals(vendorProfile.getId())) {
            throw new UnauthorizedException("You are not allowed to modify this voucher");
        }
    }


    private User getCurrentUser() {
        UserPrincipal principal = getCurrentUserPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    // ==================== CART / PRE-CHECKOUT ====================

    @Override
    @Transactional(readOnly = true)
    public VoucherClassificationResult classifyVouchersForCart(
            User user,
            List<CartItem> cartItems,
            BigDecimal totalPrice) {

        List<UserVoucher> userVouchers = userVoucherRepository.findByUser_IdAndIsUsedFalse(user.getId());
        List<UserVoucherRespone> valid = new ArrayList<>();
        List<UserVoucherRespone> invalid = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        boolean cartHasTicket   = cartItems.stream().anyMatch(i -> i.getTicketCatalog() != null);
        boolean cartHasWorkshop = cartItems.stream().anyMatch(i -> i.getWorkshopSession() != null);

        for (UserVoucher uv : userVouchers) {
            Voucher v = uv.getVoucher();
            boolean isValid = true;

            // 1. Date range
            if (v.getStartDate() != null && now.isBefore(v.getStartDate())) isValid = false;
            if (v.getEndDate()   != null && now.isAfter(v.getEndDate()))    isValid = false;

            // 2. Global usage limit
            if (v.getUsageLimit() != null && v.getUsageCount() >= v.getUsageLimit()) isValid = false;

            // 3. Min order value (based on full cart total)
            if (v.getMinOrderValue() != null && totalPrice.compareTo(v.getMinOrderValue()) < 0) isValid = false;

            // 4. applicableProduct compatibility with cart content
            if (isValid) {
                ApplicableProduct ap = v.getApplicableProduct();
                if (ap == ApplicableProduct.EVENT_TICKET && !cartHasTicket)   isValid = false;
                if (ap == ApplicableProduct.WORKSHOP      && !cartHasWorkshop) isValid = false;
                // ALL always passes
            }

            UserVoucherRespone response = UserVoucherRespone.fromEntity(uv);
            if (isValid) valid.add(response); else invalid.add(response);
        }

        return VoucherClassificationResult.builder()
                .validVouchers(valid)
                .invalidVouchers(invalid)
                .build();
    }

    @Override
    public BigDecimal applyVoucher(
            UUID userVoucherId,
            List<CartItem> cartItems,
            BigDecimal totalPrice,
            VoucherClassificationResult classification) {

        // Must be in the valid list
        UserVoucherRespone voucherResponse = classification.getValidVouchers().stream()
                .filter(v -> v.getUserVoucherId().equals(userVoucherId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Selected voucher is not applicable for this order"));

        // Reload entity to get full fields (discountType, discountValue, etc.)
        UserVoucher uv = userVoucherRepository.findById(userVoucherId)
                .orElseThrow(() -> new BadRequestException("Voucher not found"));
        Voucher v = uv.getVoucher();

        // Calculate the base amount the discount applies to (respects applicableProduct)
        BigDecimal applicableBase = switch (v.getApplicableProduct()) {
            case EVENT_TICKET -> cartItems.stream()
                    .filter(i -> i.getTicketCatalog() != null)
                    .map(i -> i.getTicketCatalog().getPrice()
                            .multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            case WORKSHOP -> cartItems.stream()
                    .filter(i -> i.getWorkshopSession() != null
                            && i.getWorkshopSession().getPrice() != null)
                    .map(i -> i.getWorkshopSession().getPrice()
                            .multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            default -> totalPrice; // ALL
        };

        // Apply discount formula
        if (v.getDiscountType() == DiscountType.PERCENT) {
            BigDecimal discount = applicableBase.multiply(
                    v.getDiscountValue().divide(BigDecimal.valueOf(100)));
            if (v.getMaxDiscountValue() != null && discount.compareTo(v.getMaxDiscountValue()) > 0) {
                discount = v.getMaxDiscountValue();
            }
            return discount;
        } else {
            // FIXED — cap at applicableBase so final price never goes below 0
            return v.getDiscountValue().min(applicableBase);
        }
    }
}
