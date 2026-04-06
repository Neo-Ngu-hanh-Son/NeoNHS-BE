package fpt.project.NeoNHS.dto.request.cart;

import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.UUID;

@Data
public class AddToCartRequest {
    private UUID ticketCatalogId;

    private UUID workshopSessionId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
