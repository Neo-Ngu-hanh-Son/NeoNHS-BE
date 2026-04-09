package fpt.project.NeoNHS.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RevenueReportResponse {
    private RevenueSummaryResponse summary;
    private List<VendorRevenueResponse> vendorBreakdown;
    private List<TransactionDetailResponse> transactions;
    private List<RevenueTrendItem> revenueTrends;
}
