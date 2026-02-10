package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.TransactionDetailResponse;
import fpt.project.NeoNHS.dto.response.TransactionResponse;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    List<TransactionResponse> getTransactions(UUID userId, String type, String status);

    TransactionDetailResponse getTransactionDetail(UUID userId, UUID transactionId);
}
