package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.TicketRepository;
import fpt.project.NeoNHS.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
}
