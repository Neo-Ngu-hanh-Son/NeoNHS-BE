package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.order.CreateOrderRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.payment.PaymentLinkResponse;
import fpt.project.NeoNHS.entity.Order;
import fpt.project.NeoNHS.entity.Transaction;
import fpt.project.NeoNHS.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;
    private final PayOS payOS;

    @Value("${app.payment.return-url:http://localhost:5173/payment/success}")
    private String returnUrl;

    @Value("${app.payment.cancel-url:http://localhost:5173/payment/cancel}")
    private String cancelUrl;

    @PostMapping("/create-payment-link")
    public ResponseEntity<ApiResponse<PaymentLinkResponse>> createPaymentLink(
            Principal principal,
            @RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(principal.getName(), request);
        Transaction transaction = order.getTransactions().get(0);

        List<ItemData> items = order.getOrderDetails().stream()
                .map(od -> ItemData.builder()
                        .name(od.getTicketCatalog().getName().length() > 50
                                ? od.getTicketCatalog().getName().substring(0, 50)
                                : od.getTicketCatalog().getName())
                        .quantity(od.getQuantity())
                        .price(od.getUnitPrice().intValue())
                        .build())
                .collect(Collectors.toList());

        int totalAmount = order.getFinalAmount().intValue();
        if (transaction.getPaymentGateway() == null || !transaction.getPaymentGateway().startsWith("PAYOS_")) {
            throw new RuntimeException("Invalid transaction payment gateway format");
        }
        long orderCode = Long.parseLong(transaction.getPaymentGateway().split("_")[1]);
        String description = "Thanh toan " + orderCode;

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(totalAmount)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .items(items)
                .build();

        try {
            CheckoutResponseData data = payOS.createPaymentLink(paymentData);

            PaymentLinkResponse response = PaymentLinkResponse.builder()
                    .checkoutUrl(data.getCheckoutUrl())
                    .orderCode(String.valueOf(orderCode))
                    .paymentLinkId(data.getPaymentLinkId())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Payment link created", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to create payment link: " + e.getMessage()));
        }
    }

    @GetMapping("/success")
    public ResponseEntity<ApiResponse<String>> handlePaymentSuccess(
            @RequestParam("orderCode") long orderCode) {

        try {
            PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(orderCode);
            if ("PAID".equals(paymentLinkData.getStatus())) {
                orderService.handlePaymentSuccess(orderCode);
                return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Payment confirmed", "Success"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, "Payment not completed or failed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error verifying payment"));
        }
    }
}
