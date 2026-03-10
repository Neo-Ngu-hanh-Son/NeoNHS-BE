package fpt.project.NeoNHS.dto.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AddToCartRequest {
    @NotNull(message = "Ticket Catalog ID cannot be null")
    private UUID ticketCatalogId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
