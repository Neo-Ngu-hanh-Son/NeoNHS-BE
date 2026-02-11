package fpt.project.NeoNHS.dto.response.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponse {
    private UUID id;
    private Integer totalItems;
    private BigDecimal totalPrice;
    private List<CartItemResponse> items;
}
