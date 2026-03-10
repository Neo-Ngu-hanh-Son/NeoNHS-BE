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
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.security.Principal;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;
    private final PayOS payOS;

    // for fun
    @Value("${app.payment.return-url:http://localhost:5173/payment/success}")
    private String returnUrl;

    @Value("${app.payment.cancel-url:http://localhost:5173/payment/cancel}")
    private String cancelUrl;

    @PostMapping("/create-payment-link")
    public ResponseEntity<ApiResponse<PaymentLinkResponse>> createPaymentLink(
            Principal principal,
            @RequestBody CreateOrderRequest request) {

        // 1. Create Order and Transaction in DB
        Order order = orderService.createOrder(principal.getName(), request);
        Transaction transaction = order.getTransactions().get(0);

        // 2. Extract orderCode from transaction (saved as "PAYOS_orderCode")
        if (transaction.getPaymentGateway() == null || !transaction.getPaymentGateway().startsWith("PAYOS_")) {
            throw new RuntimeException("Invalid transaction payment gateway format");
        }
        String paymentGateway = transaction.getPaymentGateway();
        long orderCode = Long.parseLong(paymentGateway.replace("PAYOS_", ""));

        // Use long for amount as required by PayOS SDK
        long totalAmount = order.getFinalAmount().longValue();

        // Validate amount (PayOS requires >= 2000)
        if (totalAmount < 2000) {
            totalAmount = 2000L;
        }

        // Clean description
        String description = ("Thanh toan " + orderCode).replaceAll("[^a-zA-Z0-9 ]", "").trim();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        // 3. Build PayOS Request
        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(totalAmount)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();

        try {
            // 4. Call PayOS API
            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            PaymentLinkResponse response = PaymentLinkResponse.builder()
                    .checkoutUrl(data.getCheckoutUrl())
                    .orderCode(String.valueOf(orderCode))
                    .paymentLinkId(data.getPaymentLinkId())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Payment link created successfully", response));

        } catch (Exception e) {

            return ResponseEntity.ok(ApiResponse.error(HttpStatus.BAD_REQUEST,
                    "PayOS API Error: " + e.getMessage()));
        }
    }

    @GetMapping("/success")
    public ResponseEntity<ApiResponse<String>> handlePaymentSuccess(
            @RequestParam("orderCode") long orderCode) {

        try {
            // Call PayOS to verify status
            try {
                // Use var to infer type since imports might be tricky with SDK versions
                var paymentLinkData = payOS.paymentRequests().get(orderCode);

                // Assuming paymentLinkData has getStatus() returning PaymentLinkStatus enum
                if (paymentLinkData != null && "PAID".equals(paymentLinkData.getStatus().toString())) {
                    orderService.handlePaymentSuccess(orderCode);
                    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Payment confirmed", "Success"));
                } else {
                    String status = paymentLinkData != null ? paymentLinkData.getStatus().toString() : "UNKNOWN";
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error(HttpStatus.BAD_REQUEST, "Payment status: " + status));
                }
            } catch (Exception payOsEx) {
                System.err.println("Failed to verify payment with PayOS: " + payOsEx.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Error verifying payment: " + payOsEx.getMessage()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing payment success"));
        }
    }
}
