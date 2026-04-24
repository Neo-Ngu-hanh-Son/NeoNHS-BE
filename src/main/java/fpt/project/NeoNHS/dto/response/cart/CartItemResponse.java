package fpt.project.NeoNHS.dto.response.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItemResponse {
    private UUID id;
    private UUID ticketCatalogId;
    private String itemName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;

    // Event info (for event tickets)
    private UUID eventId;
    private String eventName;

    // Workshop info (for workshop sessions)
    private UUID workshopSessionId;
    private UUID workshopTemplateId;
    private String workshopName;
}
