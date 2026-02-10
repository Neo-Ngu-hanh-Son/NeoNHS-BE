package fpt.project.NeoNHS.dto.response.ticket;

import fpt.project.NeoNHS.enums.TicketCatalogStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketCatalogResponse {
    private UUID id;
    private String name;
    private String description;
    private String customerType;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String applyOnDays;
    private LocalDateTime validFromDate;
    private LocalDateTime validToDate;
    private Integer totalQuota;
    private TicketCatalogStatus status;
}
