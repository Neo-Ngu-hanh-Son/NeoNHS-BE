package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.CheckinImageRepository;
import fpt.project.NeoNHS.service.CheckinImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckinImageServiceImpl implements CheckinImageService {

    private final CheckinImageRepository checkinImageRepository;
}
