package fpt.project.NeoNHS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailResponse {
    private UUID id;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime transactionDate;
    private UUID orderId;
    private List<TicketDetailResponse> tickets;
}
