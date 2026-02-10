package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.TicketDetailResponse;
import fpt.project.NeoNHS.dto.response.TransactionDetailResponse;
import fpt.project.NeoNHS.dto.response.TransactionResponse;
import fpt.project.NeoNHS.entity.*;
import fpt.project.NeoNHS.enums.TransactionStatus;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.repository.TransactionRepository;
import fpt.project.NeoNHS.service.TransactionService;
import fpt.project.NeoNHS.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public List<TransactionResponse> getTransactions(UUID userId, String type, String status) {
        Specification<Transaction> spec = Specification.where(TransactionSpecification.hasUserId(userId))
                .and(TransactionSpecification.hasStatus(status))
                .and(TransactionSpecification.hasType(type));

        // Sort by transactionDate desc
        Sort sort = Sort.by(Sort.Direction.DESC, "transactionDate");

        List<Transaction> transactions = transactionRepository.findAll(spec, sort);

        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
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

                            if (detail.getTicketCatalog().getValidFromDate() != null) {
                                validFrom = detail.getTicketCatalog().getValidFromDate();
                            }
                            if (detail.getTicketCatalog().getValidToDate() != null) {
                                validTo = detail.getTicketCatalog().getValidToDate();
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
        for (OrderDetail detail : transaction.getOrder().getOrderDetails()) {
            if (detail.getWorkshopSession() != null) {
                return "WORKSHOP";
            }
            if (detail.getTicketCatalog() != null) {
                // If associated with an event, it's an EVENT ticket
                if (detail.getTicketCatalog().getEvent() != null) {
                    return "EVENT";
                }
                // Otherwise defaults to ENTRANCE (e.g. Attraction tickets)
                return "ENTRANCE";
            }
        }
        return "UNKNOWN";
    }
}
