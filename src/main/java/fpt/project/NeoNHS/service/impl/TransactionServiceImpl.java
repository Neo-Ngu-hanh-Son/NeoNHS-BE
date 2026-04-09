package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.TicketDetailResponse;
import fpt.project.NeoNHS.dto.response.TransactionDetailResponse;
import fpt.project.NeoNHS.dto.response.TransactionResponse;
import fpt.project.NeoNHS.entity.*;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.repository.TransactionRepository;
import fpt.project.NeoNHS.service.TransactionService;
import fpt.project.NeoNHS.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Page<TransactionResponse> getTransactions(UUID userId, String type, String status, Pageable pageable) {
        Specification<Transaction> spec = Specification.where(TransactionSpecification.hasUserId(userId))
                .and(TransactionSpecification.hasStatus(status))
                .and(TransactionSpecification.hasType(type));

        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);

        return transactions.map(this::mapToTransactionResponse);
    }

    @Override
    public TransactionDetailResponse getTransactionDetail(UUID userId, UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getOrder().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this transaction");
        }

        List<TicketDetailResponse> ticketResponses = new ArrayList<>();
        Order order = transaction.getOrder();
        if (order != null && order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                if (detail.getTickets() != null) {
                    for (Ticket ticket : detail.getTickets()) {
                        String itemName = "";
                        LocalDateTime validFrom = ticket.getIssueDate();
                        LocalDateTime validTo = ticket.getExpiryDate();

                        if (detail.getTicketCatalog() != null) {
                            if (detail.getTicketCatalog().getEvent() != null) {
                                itemName = "Event: " + detail.getTicketCatalog().getName();
                            } else if (detail.getTicketCatalog().getAttraction() != null) {
                                itemName = "Entrance: " + detail.getTicketCatalog().getName();
                            } else {
                                itemName = detail.getTicketCatalog().getName();
                            }
                        } else if (detail.getWorkshopSession() != null) {
                            itemName = "Workshop: " + detail.getWorkshopSession().getWorkshopTemplate().getName();
                            validFrom = detail.getWorkshopSession().getStartTime();
                            validTo = detail.getWorkshopSession().getEndTime();
                        }

                        ticketResponses.add(TicketDetailResponse.builder()
                                .id(ticket.getId())
                                .ticketCode(ticket.getTicketCode())
                                .qrCode(ticket.getQrCode() != null ? ticket.getQrCode()
                                        : "https://api.qrserver.com/v1/create-qr-code/?data=" + ticket.getTicketCode())
                                .ticketType(ticket.getTicketType().name())
                                .status(ticket.getStatus().name())
                                .itemName(itemName)
                                .validFrom(validFrom)
                                .validTo(validTo)
                                .price(detail.getUnitPrice())
                                .build());
                    }
                }
            }
        }

        return TransactionDetailResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .status(transaction.getStatus().name())
                .transactionDate(transaction.getTransactionDate())
                .orderId(transaction.getOrder().getId())
                .tickets(ticketResponses)
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        String type = determineTransactionType(transaction);
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .transactionDate(transaction.getTransactionDate())
                .orderId(transaction.getOrder().getId())
                .type(type)
                .build();
    }

    private String determineTransactionType(Transaction transaction) {
        if (transaction.getOrder() == null || transaction.getOrder().getOrderDetails() == null) {
            return "UNKNOWN";
        }

        java.util.Set<String> types = new java.util.HashSet<>();

        for (OrderDetail detail : transaction.getOrder().getOrderDetails()) {
            if (detail.getWorkshopSession() != null) {
                types.add("WORKSHOP");
            } else if (detail.getTicketCatalog() != null) {
                if (detail.getTicketCatalog().getEvent() != null) {
                    types.add("EVENT");
                } else {
                    types.add("ENTRANCE");
                }
            }
        }

        if (types.size() > 1) {
            return "MIXED";
        } else if (types.size() == 1) {
            return types.iterator().next();
        }
        return "UNKNOWN";
    }
}
