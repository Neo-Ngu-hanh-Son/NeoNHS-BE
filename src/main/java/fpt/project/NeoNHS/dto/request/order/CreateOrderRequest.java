package fpt.project.NeoNHS.dto.request.order;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    private List<UUID> cartItemIds;
    private List<UUID> voucherIds; // Optional
}
