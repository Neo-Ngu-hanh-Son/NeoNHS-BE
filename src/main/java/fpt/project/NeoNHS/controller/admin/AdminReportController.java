package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.admin.ReportFilterRequest;
import fpt.project.NeoNHS.dto.request.admin.ResolveReportRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.report.ReportResponse;
import fpt.project.NeoNHS.enums.ReportStatus;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Reports", description = "Admin APIs for moderating reports")
public class AdminReportController {

    private final ReportService reportService;

    @Operation(summary = "Get all reports (Admin)", description = "Filter reports by target type (Point, Event, Workshop), status, etc.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getAllReports(
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String targetType, // Point, Workshop, Event
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) String reporterName,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .targetType(targetType)
                .status(status)
                .reporterName(reporterName)
                .build();

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ReportResponse> reports = reportService.getAllReports(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", reports));
    }

    @Operation(summary = "Get report detail")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Report retrieved successfully", reportService.getReportById(id)));
    }

    @Operation(summary = "Resolve or Reject a report")
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveReportRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ReportResponse response = reportService.resolveReport(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Report resolved successfully", response));
    }
}
