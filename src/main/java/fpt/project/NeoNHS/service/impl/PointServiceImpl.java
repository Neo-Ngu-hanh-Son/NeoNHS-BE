package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
}
