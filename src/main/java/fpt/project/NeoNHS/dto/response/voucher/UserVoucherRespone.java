package fpt.project.NeoNHS.dto.response.voucher;

import fpt.project.NeoNHS.entity.UserVoucher;
import fpt.project.NeoNHS.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucherRespone {

    private UUID userVoucherId;
    private Boolean isUsed;
    private LocalDateTime obtainedDate;
    private LocalDateTime usedDate;

    // Voucher info
    private UUID voucherId;
    private String code;
    private String description;
    private VoucherType voucherType;
    private VoucherScope scope;
    private ApplicableProduct applicableProduct;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountValue;
    private BigDecimal minOrderValue;
    private String giftDescription;
    private String giftImageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VoucherStatus status;

    private Boolean isAvailable;

    // Vendor info (for VENDOR scope)
    private UUID vendorId;
    private String vendorName;

    public static UserVoucherRespone fromEntity(UserVoucher uv) {
        var voucher = uv.getVoucher();
        UserVoucherResponeBuilder builder = UserVoucherRespone.builder()
                .userVoucherId(uv.getId())
                .isUsed(uv.getIsUsed())
                .obtainedDate(uv.getObtainedDate())
                .usedDate(uv.getUsedDate())
                .voucherId(voucher.getId())
                .code(voucher.getCode())
                .description(voucher.getDescription())
                .voucherType(voucher.getVoucherType())
                .scope(voucher.getScope())
                .applicableProduct(voucher.getApplicableProduct())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountValue(voucher.getMaxDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .giftDescription(voucher.getGiftDescription())
                .giftImageUrl(voucher.getGiftImageUrl())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .status(voucher.getStatus())
                .isAvailable(computeAvailability(uv));

        if (voucher.getVendor() != null) {
            builder.vendorId(voucher.getVendor().getId());
            builder.vendorName(voucher.getVendor().getBusinessName());
        }

        return builder.build();
    }

    private static boolean computeAvailability(UserVoucher uv) {
        if (Boolean.TRUE.equals(uv.getIsUsed())) return false;

        var voucher = uv.getVoucher();
        if (voucher.getDeletedAt() != null) return false;
        if (voucher.getStatus() != VoucherStatus.ACTIVE) return false;
        if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(LocalDateTime.now())) return false;
        if (voucher.getUsageLimit() != null && voucher.getUsageCount() >= voucher.getUsageLimit()) return false;

        return true;
    }
}
