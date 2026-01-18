package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.AttractionRepository;
import fpt.project.NeoNHS.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttractionServiceImpl implements AttractionService {

    private final AttractionRepository attractionRepository;
}
