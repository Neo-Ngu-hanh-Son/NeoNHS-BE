package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.WTagRepository;
import fpt.project.NeoNHS.service.WTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WTagServiceImpl implements WTagService {

    private final WTagRepository wTagRepository;
}
