package fpt.project.NeoNHS.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentLinkResponse {
    private String checkoutUrl;
    private String orderCode;
    private String paymentLinkId;
}
