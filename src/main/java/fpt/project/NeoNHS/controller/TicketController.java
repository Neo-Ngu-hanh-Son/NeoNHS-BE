package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.ticket.VerifyTicketRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.TicketDetailResponse;
import fpt.project.NeoNHS.service.TicketCatalogService;
import fpt.project.NeoNHS.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketCatalogService ticketCatalogService;
    private final TicketService ticketService;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> verifyTicketViaCode(
            @RequestBody VerifyTicketRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.verifyTicketViaCode(request.getCode())));
    }

}
