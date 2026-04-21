package fpt.project.NeoNHS.dto.request.voucher;

import fpt.project.NeoNHS.enums.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVoucherRequest {

    @NotBlank(message = "Voucher code is required")
    @Size(max = 50, message = "Voucher code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Voucher code can only contain letters, numbers, hyphens and underscores")
    private String code;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Voucher type is required")
    private VoucherType voucherType;

    @NotNull(message = "Applicable product type is required")
    private ApplicableProduct applicableProduct;

    // ===== Discount fields (required when voucherType = DISCOUNT) =====
    private DiscountType discountType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", inclusive = false, message = "Max discount value must be greater than 0")
    private BigDecimal maxDiscountValue;

    @DecimalMin(value = "0.0", message = "Min order value must be non-negative")
    private BigDecimal minOrderValue;

    // ===== Gift fields (for GIFT_PRODUCT type) =====
    @Size(max = 255, message = "Gift description must not exceed 255 characters")
    private String giftDescription;

    @Size(max = 255, message = "Gift image URL must not exceed 255 characters")
    private String giftImageUrl;

    // ===== Time & Usage =====
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @Positive(message = "Usage limit must be positive")
    private Integer usageLimit;
}
