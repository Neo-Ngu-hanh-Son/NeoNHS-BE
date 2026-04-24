package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.payout.CreatePayoutRequest;
import fpt.project.NeoNHS.dto.response.payout.PayoutResponse;
import fpt.project.NeoNHS.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.payos.PayOS;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutServiceImpl implements PayoutService {

    private final PayOS payOS;

    @Value("${payos.return.client_id}")
    private String payosClientId;

    @Value("${payos.return.api_key}")
    private String payosApiKey;

    @Value("${payos.return.checksum_key}")
    private String payosChecksumKey;

    private static final String PAYOS_API_BASE_URL = "https://api-merchant.payos.vn";
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public PayoutResponse createPayout(CreatePayoutRequest request) {
        log.info("Creating payout with referenceId: {}", request.getReferenceId());

        try {
            Map<String, Object> payoutData = new HashMap<>();
            payoutData.put("referenceId", request.getReferenceId());
            payoutData.put("amount", request.getAmount());
            payoutData.put("description", request.getDescription());
            payoutData.put("toBin", request.getToBin());
            payoutData.put("toAccountNumber", request.getToAccountNumber());
            if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                payoutData.put("category", request.getCategory());
            }

            String signature = payOS.getCrypto().createSignature(payosChecksumKey, payoutData);

            String idempotencyKey = UUID.randomUUID().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", payosClientId);
            headers.set("x-api-key", payosApiKey);
            headers.set("x-idempotency-key", idempotencyKey);
            headers.set("x-signature", signature);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payoutData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    PAYOS_API_BASE_URL + "/v1/payouts",
                    HttpMethod.POST,
                    entity,
                    Map.class);

            Map<String, Object> payosResponse = response.getBody();

            if (payosResponse != null && "00".equals(payosResponse.get("code"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) payosResponse.get("data");

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
                                ? OffsetDateTime.parse((String) data.get("createdAt")).toLocalDateTime()
                                : LocalDateTime.now())
                        .build();

                return payoutResponse;
            } else {
                String errorCode = payosResponse != null ? String.valueOf(payosResponse.get("code")) : "N/A";
                String errorMsg = payosResponse != null ? (String) payosResponse.get("desc") : "Unknown error";
                throw new RuntimeException("PayOS error [" + errorCode + "]: " + errorMsg);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payout: " + e.getMessage(), e);
        }
    }

    @Override
    public PayoutResponse getPayoutById(String payoutId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", payosClientId);
            headers.set("x-api-key", payosApiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    PAYOS_API_BASE_URL + "/v1/payouts/" + payoutId,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            Map<String, Object> payosResponse = response.getBody();

            if (payosResponse != null && "00".equals(payosResponse.get("code"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) payosResponse.get("data");

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
                                ? OffsetDateTime.parse((String) data.get("createdAt")).toLocalDateTime()
                                : LocalDateTime.now())
                        .build();

                return payoutResponse;
            } else {
                String errorMsg = payosResponse != null ? (String) payosResponse.get("desc") : "Unknown error";
                throw new RuntimeException("PayOS error: " + errorMsg);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get payout: " + e.getMessage(), e);
        }
    }
}
