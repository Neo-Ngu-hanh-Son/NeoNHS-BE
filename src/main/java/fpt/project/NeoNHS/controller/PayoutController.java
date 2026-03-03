package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.payout.CreatePayoutRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.payout.PayoutResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import vn.payos.PayOS;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payouts")
@RequiredArgsConstructor
public class PayoutController {

        private final PayOS payOS; // Inject PayOS bean từ PayOSConfig

        @Value("${payos.return.client-id}")
        private String payosClientId;

        @Value("${payos.return.api-key}")
        private String payosApiKey;

        @Value("${payos.return.checksum-key}")
        private String payosChecksumKey;

        private static final String PAYOS_API_BASE_URL = "https://api-merchant.payos.vn";
        private final RestTemplate restTemplate = new RestTemplate();

        @PostMapping
        public ResponseEntity<ApiResponse<PayoutResponse>> createPayout(
                        @Valid @RequestBody CreatePayoutRequest request) {

                log.info("Creating payout with referenceId: {}", request.getReferenceId());

                try {
                        // Tạo request body cho PayOS API
                        Map<String, Object> payoutData = new HashMap<>();
                        payoutData.put("referenceId", request.getReferenceId());
                        payoutData.put("amount", request.getAmount());
                        payoutData.put("description", request.getDescription());
                        payoutData.put("toBin", request.getToBin());
                        payoutData.put("toAccountNumber", request.getToAccountNumber());
                        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                                payoutData.put("category", request.getCategory());
                        }

                        // Tạo signature sử dụng PayOS SDK's crypto utility
                        // Method: createSignature(checksumKey, data)
                        String signature = payOS.getCrypto().createSignature(payosChecksumKey, payoutData);

                        log.info("Generated signature: {}", signature);

                        // Tạo idempotency key
                        String idempotencyKey = UUID.randomUUID().toString();

                        // Setup headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set("x-client-id", payosClientId);
                        headers.set("x-api-key", payosApiKey);
                        headers.set("x-idempotency-key", idempotencyKey);
                        headers.set("x-signature", signature);

                        log.info("Request headers: client-id={}, api-key={}, idempotency-key={}, signature={}",
                                        payosClientId, payosApiKey.substring(0, 10) + "...", idempotencyKey, signature);

                        // Tạo HTTP request
                        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payoutData, headers);

                        // Gọi PayOS API
                        ResponseEntity<Map> response = restTemplate.exchange(
                                        PAYOS_API_BASE_URL + "/v1/payouts",
                                        HttpMethod.POST,
                                        entity,
                                        Map.class);

                        Map<String, Object> payosResponse = response.getBody();

                        // Parse response
                        if (payosResponse != null && "00".equals(payosResponse.get("code"))) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> data = (Map<String, Object>) payosResponse.get("data");

                                // Convert transactions
                                List<PayoutResponse.PayoutTransaction> transactions = new ArrayList<>();
                                Object txnsObj = data.get("transactions");

                                if (txnsObj instanceof List) {
                                        @SuppressWarnings("unchecked")
                                        List<Map<String, Object>> txnsList = (List<Map<String, Object>>) txnsObj;
                                        for (Map<String, Object> txn : txnsList) {
                                                PayoutResponse.PayoutTransaction transaction = PayoutResponse.PayoutTransaction
                                                                .builder()
                                                                .id((String) txn.get("id"))
                                                                .referenceId((String) txn.get("referenceId"))
                                                                .amount((Integer) txn.get("amount"))
                                                                .description((String) txn.get("description"))
                                                                .toBin((String) txn.get("toBin"))
                                                                .toAccountNumber((String) txn.get("toAccountNumber"))
                                                                .toAccountName((String) txn.get("toAccountName"))
                                                                .state((String) txn.get("state"))
                                                                .build();
                                                transactions.add(transaction);
                                        }
                                }

                                // Build response
                                @SuppressWarnings("unchecked")
                                PayoutResponse payoutResponse = PayoutResponse.builder()
                                                .id((String) data.get("id"))
                                                .referenceId((String) data.get("referenceId"))
                                                .transactions(transactions)
                                                .category((List<String>) data.get("category"))
                                                .approvalState((String) data.get("approvalState"))
                                                .createdAt(data.get("createdAt") != null
                                                                ? OffsetDateTime.parse((String) data.get("createdAt"))
                                                                                .toLocalDateTime()
                                                                : LocalDateTime.now())
                                                .build();

                                log.info("Payout created successfully: {}", payoutResponse.getId());

                                return ResponseEntity.ok(
                                                ApiResponse.success(HttpStatus.OK, "Payout created successfully",
                                                                payoutResponse));
                        } else {
                                String errorCode = payosResponse != null ? String.valueOf(payosResponse.get("code"))
                                                : "N/A";
                                String errorMsg = payosResponse != null ? (String) payosResponse.get("desc")
                                                : "Unknown error";
                                log.error("PayOS API error - code: {}, desc: {}, full response: {}",
                                                errorCode, errorMsg, payosResponse);
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(ApiResponse.error(HttpStatus.BAD_REQUEST,
                                                                "PayOS error [" + errorCode + "]: " + errorMsg));
                        }

                } catch (Exception e) {
                        log.error("Error creating payout: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Failed to create payout: " + e.getMessage()));
                }
        }

        @GetMapping("/{payoutId}")
        public ResponseEntity<ApiResponse<PayoutResponse>> getPayoutById(
                        @PathVariable String payoutId) {

                log.info("Getting payout details for ID: {}", payoutId);

                try {
                        // Setup headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("x-client-id", payosClientId);
                        headers.set("x-api-key", payosApiKey);

                        HttpEntity<Void> entity = new HttpEntity<>(headers);

                        // Gọi PayOS API
                        ResponseEntity<Map> response = restTemplate.exchange(
                                        PAYOS_API_BASE_URL + "/v1/payouts/" + payoutId,
                                        HttpMethod.GET,
                                        entity,
                                        Map.class);

                        Map<String, Object> payosResponse = response.getBody();

                        if (payosResponse != null && "00".equals(payosResponse.get("code"))) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> data = (Map<String, Object>) payosResponse.get("data");

                                // Convert transactions
                                List<PayoutResponse.PayoutTransaction> transactions = new ArrayList<>();
                                Object txnsObj = data.get("transactions");

                                if (txnsObj instanceof List) {
                                        @SuppressWarnings("unchecked")
                                        List<Map<String, Object>> txnsList = (List<Map<String, Object>>) txnsObj;
                                        for (Map<String, Object> txn : txnsList) {
                                                PayoutResponse.PayoutTransaction transaction = PayoutResponse.PayoutTransaction
                                                                .builder()
                                                                .id((String) txn.get("id"))
                                                                .referenceId((String) txn.get("referenceId"))
                                                                .amount((Integer) txn.get("amount"))
                                                                .description((String) txn.get("description"))
                                                                .toBin((String) txn.get("toBin"))
                                                                .toAccountNumber((String) txn.get("toAccountNumber"))
                                                                .toAccountName((String) txn.get("toAccountName"))
                                                                .state((String) txn.get("state"))
                                                                .build();
                                                transactions.add(transaction);
                                        }
                                }

                                @SuppressWarnings("unchecked")
                                PayoutResponse payoutResponse = PayoutResponse.builder()
                                                .id((String) data.get("id"))
                                                .referenceId((String) data.get("referenceId"))
                                                .transactions(transactions)
                                                .category((List<String>) data.get("category"))
                                                .approvalState((String) data.get("approvalState"))
                                                .createdAt(data.get("createdAt") != null
                                                                ? OffsetDateTime.parse((String) data.get("createdAt"))
                                                                                .toLocalDateTime()
                                                                : LocalDateTime.now())
                                                .build();

                                return ResponseEntity.ok(
                                                ApiResponse.success(HttpStatus.OK, "Payout retrieved successfully",
                                                                payoutResponse));
                        } else {
                                String errorMsg = payosResponse != null ? (String) payosResponse.get("desc")
                                                : "Unknown error";
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(ApiResponse.error(HttpStatus.NOT_FOUND,
                                                                "PayOS error: " + errorMsg));
                        }

                } catch (Exception e) {
                        log.error("Error getting payout: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Failed to get payout: " + e.getMessage()));
                }
        }

        @PostMapping("/test")
        public ResponseEntity<ApiResponse<PayoutResponse>> testPayout() {

                log.info("Testing payout with hardcoded data");

                // Hardcoded test data theo yêu cầu
                CreatePayoutRequest testRequest = CreatePayoutRequest.builder()
                                .referenceId("test_payout_" + System.currentTimeMillis())
                                .amount(5000)
                                .description("Test payment")
                                .toBin("970422") // MB Bank
                                .toAccountNumber("0935062645")
                                .category(Collections.singletonList("test"))
                                .build();

                // Gọi lại endpoint chính
                return createPayout(testRequest);
        }
}
