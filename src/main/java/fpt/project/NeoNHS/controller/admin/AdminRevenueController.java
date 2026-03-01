package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.request.admin.RevenueReportRequest;
import fpt.project.NeoNHS.dto.response.admin.RevenueReportResponse;
import fpt.project.NeoNHS.service.RevenueAnalyticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Revenue", description = "Admin APIs for managing revenue (requires ADMIN role)")
public class AdminRevenueController {
    private final RevenueAnalyticsService revenueAnalyticsService;

    @GetMapping("/revenue-report")
    public ResponseEntity<RevenueReportResponse> getReport(RevenueReportRequest request) {
        return ResponseEntity.ok(revenueAnalyticsService.getFullReport(request));
    }
}
