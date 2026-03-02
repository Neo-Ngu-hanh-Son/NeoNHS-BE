package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import fpt.project.NeoNHS.dto.response.admin.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.admin.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Dashboard", description = "Admin APIs for dashboard statistics (requires ADMIN role)")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get KPI Overview", description = "Retrieve core metrics like total users, vendors, and revenue")
    @GetMapping("/kpi")
    public ResponseEntity<ApiResponse<KpiOverviewResponse>> getKpiOverview() {
        KpiOverviewResponse data = dashboardService.getKpiOverview();
        return ResponseEntity.ok(ApiResponse.success("KPI data retrieved successfully", data));
    }

    @Operation(summary = "Get Revenue Trends", description = "Get revenue trends by MONTHLY or WEEKLY")
    @GetMapping("/revenue-trends")
    public ResponseEntity<ApiResponse<RevenueTrendsResponse>> getRevenueTrends(
            @RequestParam(defaultValue = "MONTHLY") String periodType,
            @RequestParam(defaultValue = "6") Integer limit) {
        RevenueTrendsResponse data = dashboardService.getRevenueTrends(periodType, limit);
        return ResponseEntity.ok(ApiResponse.success("Revenue trends retrieved successfully", data));
    }

    @Operation(summary = "Get Activity Status Distribution", description = "Count workshops and events by their status")
    @GetMapping("/activity-status")
    public ResponseEntity<ApiResponse<StatusCountResponse>> getActivityStatus() {
        StatusCountResponse data = dashboardService.getActivityStatus();
        return ResponseEntity.ok(ApiResponse.success("Activity status stats retrieved successfully", data));
    }

    @Operation(summary = "Get Sales Distribution", description = "Comparison between Workshop sales and Event sales")
    @GetMapping("/sales-by-type")
    public ResponseEntity<ApiResponse<SalesByTypeResponse>> getSalesByType() {
        SalesByTypeResponse data = dashboardService.getSalesByType();
        return ResponseEntity.ok(ApiResponse.success("Sales distribution retrieved successfully", data));
    }

    @Operation(summary = "Get Top Activities", description = "Get most popular workshops/events by ticket sales")
    @GetMapping("/top-activities")
    public ResponseEntity<ApiResponse<List<TopActivityResponse>>> getTopActivities(
            @RequestParam String type,
            @RequestParam(defaultValue = "5") Integer limit) {
        List<TopActivityResponse> data = dashboardService.getTopActivities(type, limit);
        return ResponseEntity.ok(ApiResponse.success("Top activities retrieved successfully", data));
    }

    @Operation(summary = "Get Registration Stats", description = "Growth stats for Users or Vendors")
    @GetMapping("/registrations")
    public ResponseEntity<ApiResponse<List<RegistrationStatResponse>>> getRegistrations(
            @RequestParam String type,
            @RequestParam(defaultValue = "MONTHLY") String periodType,
            @RequestParam(defaultValue = "6") Integer limit) {
        List<RegistrationStatResponse> data = dashboardService.getRegistrations(type, periodType, limit);
        return ResponseEntity.ok(ApiResponse.success("Registration statistics retrieved successfully", data));
    }

    @Operation(
            summary = "Get Recent Vendor Activity",
            description = "Recent activities performed by vendors (derived from workshops, events, tickets)"
    )
    @GetMapping("/recent-activities")
    public ResponseEntity<ApiResponse<List<VendorActivityResponse>>> getRecentVendorActivities(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<VendorActivityResponse> data = dashboardService.getRecentVendorActivities(limit);
        return ResponseEntity.ok(
                ApiResponse.success("Recent vendor activities retrieved successfully", data)
        );
    }
}