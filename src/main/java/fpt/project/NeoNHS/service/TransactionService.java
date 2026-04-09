package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.TransactionDetailResponse;
import fpt.project.NeoNHS.dto.response.TransactionResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    Page<TransactionResponse> getTransactions(UUID userId, String type, String status, Pageable pageable);

    TransactionDetailResponse getTransactionDetail(UUID userId, UUID transactionId);
}
