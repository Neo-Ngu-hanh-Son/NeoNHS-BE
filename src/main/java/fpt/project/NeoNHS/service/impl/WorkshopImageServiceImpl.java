package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.WorkshopImageRepository;
import fpt.project.NeoNHS.service.WorkshopImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkshopImageServiceImpl implements WorkshopImageService {

    private final WorkshopImageRepository workshopImageRepository;
}
