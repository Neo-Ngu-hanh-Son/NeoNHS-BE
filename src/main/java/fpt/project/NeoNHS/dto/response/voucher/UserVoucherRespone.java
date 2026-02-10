package fpt.project.NeoNHS.dto.response.voucher;

import java.math.BigDecimal;
import java.util.UUID;

import fpt.project.NeoNHS.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucherRespone {
    private UUID userVoucherId;
    private String code;
    private String description;
    private BigDecimal discountValue;
    private DiscountType type;
    private BigDecimal minOrderValue;
}
