package fpt.project.NeoNHS.dto.response.admin;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevenueTrendItem {
    private String period;
    private BigDecimal revenue;
}
