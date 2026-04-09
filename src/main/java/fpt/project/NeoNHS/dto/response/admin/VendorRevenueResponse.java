package fpt.project.NeoNHS.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorRevenueResponse {
    private String vendorName;
    private BigDecimal amount;
    private Double percentage;
}
