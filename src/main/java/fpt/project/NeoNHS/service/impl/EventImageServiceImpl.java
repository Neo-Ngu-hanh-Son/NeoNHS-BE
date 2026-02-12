package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.EventImageRepository;
import fpt.project.NeoNHS.service.EventImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventImageServiceImpl implements EventImageService {

    private final EventImageRepository eventImageRepository;
}
