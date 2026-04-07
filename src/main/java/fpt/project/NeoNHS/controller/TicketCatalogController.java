package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.service.TicketCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ticket-catalogs")
@RequiredArgsConstructor
public class TicketCatalogController {

    private final TicketCatalogService ticketCatalogService;

    // @GetMapping("/event/{eventId}")
    // public ResponseEntity<ApiResponse<List<TicketCatalogResponse>>> getByEvent(@PathVariable UUID eventId) {
    //     List<TicketCatalogResponse> data = ticketCatalogService.getTicketCatalogsByEvent(eventId);
    //     return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Get ticket catalogs by event successfully", data));
    // }

    // @GetMapping("/attraction/{attractionId}")
    // public ResponseEntity<ApiResponse<List<TicketCatalogResponse>>> getByAttraction(@PathVariable UUID attractionId) {
    //     List<TicketCatalogResponse> data = ticketCatalogService.getTicketCatalogsByAttraction(attractionId);
    //     return ResponseEntity
    //             .ok(ApiResponse.success(HttpStatus.OK, "Get ticket catalogs by attraction successfully", data));
    // }
}
