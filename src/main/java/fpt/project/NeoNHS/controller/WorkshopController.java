package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.service.WorkshopSessionService;
import fpt.project.NeoNHS.service.WorkshopTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workshops")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopTemplateService workshopTemplateService;
    private final WorkshopSessionService workshopSessionService;
}
