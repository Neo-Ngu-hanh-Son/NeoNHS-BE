package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.admin.ReportFilterRequest;
import fpt.project.NeoNHS.dto.request.admin.ResolveReportRequest;
import fpt.project.NeoNHS.dto.request.report.CreateReportRequest;
import fpt.project.NeoNHS.dto.response.report.ReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReportService {
    //Admin
    Page<ReportResponse> getAllReports(ReportFilterRequest filter, Pageable pageable);
    ReportResponse getReportById(UUID id);
    ReportResponse resolveReport(UUID id, ResolveReportRequest request, UUID adminId);

    //User
    ReportResponse createReport(CreateReportRequest request, UUID reporterId);
}
