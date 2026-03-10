package fpt.project.NeoNHS.dto.request.ticketcatalog;

import fpt.project.NeoNHS.enums.TicketCatalogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
public class CreateTicketCatalogRequest {

    @NotBlank(message = "Ticket catalog name is required")
    private String name;

    private String description;

    private String customerType;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String applyOnDays;

    private LocalDateTime validFromDate;

    private LocalDateTime validToDate;

    @PositiveOrZero(message = "Total quota must be 0 or greater")
    private Integer totalQuota;

    private TicketCatalogStatus status;
}
