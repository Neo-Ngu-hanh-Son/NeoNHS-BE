package fpt.project.NeoNHS.controller.vendor;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.vendor.dashboard.*;
import fpt.project.NeoNHS.service.VendorDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendor/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
@Tag(name = "Vendor - Dashboard", description = "Vendor APIs for dashboard data (requires VENDOR role)")
public class VendorDashboardController {

    private final VendorDashboardService vendorDashboardService;

    @Operation(summary = "Get dashboard stats", description = "Revenue, Workshops, Bookings, Vouchers counts with trend %")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<VendorStatsResponse>> getStats(
            @Parameter(description = "IANA timezone, e.g. Asia/Ho_Chi_Minh")
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone) {
        VendorStatsResponse data = vendorDashboardService.getStats(timezone);
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved successfully", data));
    }

    @Operation(summary = "Get revenue chart data", description = "Revenue series points filtered by week, month, or year")
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<VendorRevenueSeriesResponse>> getRevenue(
            @Parameter(description = "Range: week | month | year")
            @RequestParam(defaultValue = "week") String range,
            @Parameter(description = "IANA timezone, e.g. Asia/Ho_Chi_Minh")
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone) {
        VendorRevenueSeriesResponse data = vendorDashboardService.getRevenue(range, timezone);
        return ResponseEntity.ok(ApiResponse.success("Revenue data retrieved successfully", data));
    }

    @Operation(summary = "Get workshop status distribution", description = "Count of workshops by status (Active, Pending, Draft, Rejected)")
    @GetMapping("/workshop-status")
    public ResponseEntity<ApiResponse<List<VendorWorkshopStatusItem>>> getWorkshopStatus() {
        List<VendorWorkshopStatusItem> data = vendorDashboardService.getWorkshopStatus();
        return ResponseEntity.ok(ApiResponse.success("Workshop status retrieved successfully", data));
    }

    @Operation(summary = "Get recent transactions", description = "Recent successful transactions for the vendor's workshops")
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<VendorTransactionItem>>> getTransactions(
            @Parameter(description = "Max number of transactions to return")
            @RequestParam(defaultValue = "10") int limit) {
        List<VendorTransactionItem> data = vendorDashboardService.getTransactions(limit);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", data));
    }

    @Operation(summary = "Get workshop reviews summary", description = "Review stats per workshop: total, average rating, new reviews this week")
    @GetMapping("/workshop-reviews")
    public ResponseEntity<ApiResponse<List<VendorWorkshopReviewItem>>> getWorkshopReviews(
            @Parameter(description = "IANA timezone, e.g. Asia/Ho_Chi_Minh")
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone,
            @Parameter(description = "Max number of workshops to return")
            @RequestParam(defaultValue = "10") int limit) {
        List<VendorWorkshopReviewItem> data = vendorDashboardService.getWorkshopReviews(timezone, limit);
        return ResponseEntity.ok(ApiResponse.success("Workshop reviews retrieved successfully", data));
    }

    @Operation(summary = "Get session calendar data", description = "Sessions grouped by date with highlight dates for calendar display")
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<VendorSessionsResponse>> getSessions(
            @Parameter(description = "Start date (YYYY-MM-DD). Defaults to first day of current month")
            @RequestParam(required = false) String from,
            @Parameter(description = "End date (YYYY-MM-DD). Defaults to first day of next month")
            @RequestParam(required = false) String to,
            @Parameter(description = "IANA timezone, e.g. Asia/Ho_Chi_Minh")
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone) {
        VendorSessionsResponse data = vendorDashboardService.getSessions(from, to, timezone);
        return ResponseEntity.ok(ApiResponse.success("Sessions retrieved successfully", data));
    }
}
