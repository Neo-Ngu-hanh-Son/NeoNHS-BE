package fpt.project.NeoNHS.dto.response.admin;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KpiOverviewResponse {
    private long totalUsers;
    private long activeVendors;
    private long ticketsSold;
    private BigDecimal revenue;
}
