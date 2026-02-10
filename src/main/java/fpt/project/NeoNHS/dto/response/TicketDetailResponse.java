package fpt.project.NeoNHS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponse {
    private UUID id;
    private String ticketCode;
    private String qrCode;
    private String ticketType; // EVENT | WORKSHOP | VISITOR
    private String status;
    private String itemName;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}
