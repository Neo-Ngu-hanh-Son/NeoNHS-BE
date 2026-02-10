package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.TicketDetailResponse;

public interface TicketService {
    TicketDetailResponse verifyTicketViaCode(String code);
}
