package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.WorkshopSessionRepository;
import fpt.project.NeoNHS.service.WorkshopSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkshopSessionServiceImpl implements WorkshopSessionService {

    private final WorkshopSessionRepository workshopSessionRepository;
}
