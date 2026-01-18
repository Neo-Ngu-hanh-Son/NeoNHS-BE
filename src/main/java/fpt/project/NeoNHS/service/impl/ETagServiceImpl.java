package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.ETagRepository;
import fpt.project.NeoNHS.service.ETagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ETagServiceImpl implements ETagService {

    private final ETagRepository eTagRepository;
}
