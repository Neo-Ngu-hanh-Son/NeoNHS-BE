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
public class UpdateVoucherRequest {

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private ApplicableProduct applicableProduct;

    // ===== Discount fields =====
    private DiscountType discountType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", inclusive = false, message = "Max discount value must be greater than 0")
    private BigDecimal maxDiscountValue;

    @DecimalMin(value = "0.0", message = "Min order value must be non-negative")
    private BigDecimal minOrderValue;

    // ===== Gift fields =====
    @Size(max = 255, message = "Gift description must not exceed 255 characters")
    private String giftDescription;

    @Size(max = 255, message = "Gift image URL must not exceed 255 characters")
    private String giftImageUrl;

    // ===== Time & Usage =====
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDateTime startDate;

    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @Positive(message = "Usage limit must be positive")
    private Integer usageLimit;

    private VoucherStatus status;
}
