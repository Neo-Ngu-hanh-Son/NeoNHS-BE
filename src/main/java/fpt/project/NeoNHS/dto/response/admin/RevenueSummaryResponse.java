package fpt.project.NeoNHS.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RevenueSummaryResponse {
    private BigDecimal totalGross;        // Tổng tiền chảy qua hệ thống
    private BigDecimal adminEarnings;     // Tiền Admin thực sự kiếm được (Phí + Vé Admin)
    private BigDecimal vendorPayouts;   // Tổng tiền Vendor thực nhận
    private Long totalTransactions;
}
