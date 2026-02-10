package fpt.project.NeoNHS.dto.request.cart;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class CheckoutRequest {
    private List<UUID> cartItemIds;
    private List<UUID> voucherIds;

}
