package fpt.project.NeoNHS.dto.response.vendor.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRevenuePoint {
    private String label;
    private BigDecimal revenue; // Tổng tiền transaction
    private BigDecimal netAmount; // Tiền vendor thực nhận (sau commission)
}
