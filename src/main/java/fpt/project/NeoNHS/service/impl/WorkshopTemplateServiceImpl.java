package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.service.WorkshopTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkshopTemplateServiceImpl implements WorkshopTemplateService {

    private final WorkshopTemplateRepository workshopTemplateRepository;
}
