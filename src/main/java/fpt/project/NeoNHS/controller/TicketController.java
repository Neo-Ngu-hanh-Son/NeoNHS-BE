package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.service.TicketCatalogService;
import fpt.project.NeoNHS.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketCatalogService ticketCatalogService;
    private final TicketService ticketService;
}
