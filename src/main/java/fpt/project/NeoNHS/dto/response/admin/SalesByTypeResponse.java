package fpt.project.NeoNHS.dto.response.admin;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesByTypeResponse {

    private Type workshop;
    private Type event;

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Type {
        private long ticketsSold;
        private BigDecimal revenue;
    }
}
