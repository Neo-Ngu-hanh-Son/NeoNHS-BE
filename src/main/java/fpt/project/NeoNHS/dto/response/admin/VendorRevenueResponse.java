package fpt.project.NeoNHS.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class VendorRevenueResponse {
    private String vendorName;
    private BigDecimal amount;
}
