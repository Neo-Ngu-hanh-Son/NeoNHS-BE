package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.TicketCatalogRepository;
import fpt.project.NeoNHS.service.TicketCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketCatalogServiceImpl implements TicketCatalogService {

    private final TicketCatalogRepository ticketCatalogRepository;
}
