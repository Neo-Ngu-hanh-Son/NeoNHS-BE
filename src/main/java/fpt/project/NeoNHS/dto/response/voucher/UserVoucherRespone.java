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
    private Integer bonusPointsValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VoucherStatus status;

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
                .bonusPointsValue(voucher.getBonusPointsValue())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .status(voucher.getStatus());

        if (voucher.getVendor() != null) {
            builder.vendorId(voucher.getVendor().getId());
            builder.vendorName(voucher.getVendor().getBusinessName());
        }

        return builder.build();
    }
}
