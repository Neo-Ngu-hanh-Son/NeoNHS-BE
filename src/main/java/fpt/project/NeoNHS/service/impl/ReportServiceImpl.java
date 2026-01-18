package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.ReportRepository;
import fpt.project.NeoNHS.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
}
