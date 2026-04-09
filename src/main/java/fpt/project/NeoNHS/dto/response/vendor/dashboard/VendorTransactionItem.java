package fpt.project.NeoNHS.dto.response.vendor.dashboard;

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
public class VendorTransactionItem {
    private String id;
    private String workshopName;
    private String customerName;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paidAt;
    private String status;
}
