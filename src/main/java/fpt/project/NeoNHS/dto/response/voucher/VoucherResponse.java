package fpt.project.NeoNHS.dto.response.voucher;

import fpt.project.NeoNHS.entity.Voucher;
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
public class VoucherResponse {

    private UUID id;
    private String code;
    private String description;

    // Type & Scope
    private VoucherType voucherType;
    private VoucherScope scope;
    private ApplicableProduct applicableProduct;

    // Discount
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountValue;
    private BigDecimal minOrderValue;

    // Gift
    private String giftDescription;
    private String giftImageUrl;

    // Bonus points
    private Integer bonusPointsValue;

    // Free service
    private UUID freeTicketCatalogId;
    private String freeTicketCatalogName;

    // Time & Usage
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer maxUsagePerUser;
    private VoucherStatus status;

    // Creator info
    private UUID createdByUserId;
    private String createdByUserName;
    private UUID vendorId;
    private String vendorName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static VoucherResponse fromEntity(Voucher voucher) {
        VoucherResponseBuilder builder = VoucherResponse.builder()
                .id(voucher.getId())
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
                .usageLimit(voucher.getUsageLimit())
                .usageCount(voucher.getUsageCount())
                .maxUsagePerUser(voucher.getMaxUsagePerUser())
                .status(voucher.getStatus())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .deletedAt(voucher.getDeletedAt());

        if (voucher.getCreatedByUser() != null) {
            builder.createdByUserId(voucher.getCreatedByUser().getId());
            builder.createdByUserName(voucher.getCreatedByUser().getFullname());
        }

        if (voucher.getVendor() != null) {
            builder.vendorId(voucher.getVendor().getId());
            builder.vendorName(voucher.getVendor().getBusinessName());
        }

        if (voucher.getFreeTicketCatalog() != null) {
            builder.freeTicketCatalogId(voucher.getFreeTicketCatalog().getId());
            builder.freeTicketCatalogName(voucher.getFreeTicketCatalog().getName());
        }

        return builder.build();
    }
}
