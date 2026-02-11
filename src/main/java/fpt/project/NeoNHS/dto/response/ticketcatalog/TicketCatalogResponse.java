package fpt.project.NeoNHS.dto.response.ticketcatalog;

import fpt.project.NeoNHS.entity.TicketCatalog;
import fpt.project.NeoNHS.enums.TicketCatalogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCatalogResponse {

    private UUID id;

    private UUID eventId;

    private String name;

    private String description;

    private String customerType;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String applyOnDays;

    private LocalDateTime validFromDate;

    private LocalDateTime validToDate;

    private Integer totalQuota;

    private Integer soldQuantity;

    private Integer remainingQuantity;

    private TicketCatalogStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private UUID deletedBy;

    public static TicketCatalogResponse fromEntity(TicketCatalog ticketCatalog) {
        Integer totalQuota = ticketCatalog.getTotalQuota();
        Integer soldQuantity = ticketCatalog.getSoldQuantity() != null ? ticketCatalog.getSoldQuantity() : 0;
        Integer remainingQuantity = totalQuota != null ? totalQuota - soldQuantity : null;

        return TicketCatalogResponse.builder()
                .id(ticketCatalog.getId())
                .eventId(ticketCatalog.getEvent() != null ? ticketCatalog.getEvent().getId() : null)
                .name(ticketCatalog.getName())
                .description(ticketCatalog.getDescription())
                .customerType(ticketCatalog.getCustomerType())
                .price(ticketCatalog.getPrice())
                .originalPrice(ticketCatalog.getOriginalPrice())
                .applyOnDays(ticketCatalog.getApplyOnDays())
                .validFromDate(ticketCatalog.getValidFromDate())
                .validToDate(ticketCatalog.getValidToDate())
                .totalQuota(totalQuota)
                .soldQuantity(soldQuantity)
                .remainingQuantity(remainingQuantity)
                .status(ticketCatalog.getStatus())
                .createdAt(ticketCatalog.getCreatedAt())
                .updatedAt(ticketCatalog.getUpdatedAt())
                .deletedAt(ticketCatalog.getDeletedAt())
                .deletedBy(ticketCatalog.getDeletedBy())
                .build();
    }
}
