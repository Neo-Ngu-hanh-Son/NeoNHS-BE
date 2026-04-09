package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.response.TransactionDetailResponse;
import fpt.project.NeoNHS.dto.response.TransactionResponse;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

        private final TransactionService transactionService;

        @GetMapping
        public ResponseEntity<Page<TransactionResponse>> getTransactions(
                        @RequestParam(required = false) String type,
                        @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
                Page<TransactionResponse> response = transactionService.getTransactions(userPrincipal.getId(), type,
                                status,
                                pageable);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{transactionId}")
        public ResponseEntity<TransactionDetailResponse> getTransactionDetail(@PathVariable UUID transactionId) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                TransactionDetailResponse response = transactionService.getTransactionDetail(userPrincipal.getId(),
                                transactionId);
                return ResponseEntity.ok(response);
        }
}
