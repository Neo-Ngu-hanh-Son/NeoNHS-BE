package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.vendor.dashboard.*;

import java.util.List;

public interface VendorDashboardService {

    VendorStatsResponse getStats(String timezone);

    VendorRevenueSeriesResponse getRevenue(String range, String timezone);

    List<VendorWorkshopStatusItem> getWorkshopStatus();

    List<VendorTransactionItem> getTransactions(int limit);

    List<VendorWorkshopReviewItem> getWorkshopReviews(String timezone, int limit);

    VendorSessionsResponse getSessions(String from, String to, String timezone);
}
