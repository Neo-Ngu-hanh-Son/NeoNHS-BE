package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.CheckinPointRepository;
import fpt.project.NeoNHS.service.CheckinPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckinPointServiceImpl implements CheckinPointService {

    private final CheckinPointRepository checkinPointRepository;
}
