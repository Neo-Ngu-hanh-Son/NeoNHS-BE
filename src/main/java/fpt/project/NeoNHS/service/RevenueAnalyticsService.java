package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.admin.RevenueReportRequest;
import fpt.project.NeoNHS.dto.response.admin.RevenueReportResponse;

public interface RevenueAnalyticsService {
    RevenueReportResponse getFullReport(RevenueReportRequest request);
}