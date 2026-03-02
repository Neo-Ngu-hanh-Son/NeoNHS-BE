package fpt.project.NeoNHS.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDetailResponse {
    private LocalDateTime date;
    private String id;
    private String vendor;
    private String item;
    private BigDecimal gross;
    private BigDecimal fee;
    private BigDecimal net;
    private String status;
}
