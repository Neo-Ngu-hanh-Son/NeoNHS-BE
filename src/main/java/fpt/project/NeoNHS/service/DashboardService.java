package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.admin.*;
import java.util.List;
        
public interface DashboardService {

    KpiOverviewResponse getKpiOverview();

    RevenueTrendsResponse getRevenueTrends(String periodType, Integer limit);

    StatusCountResponse getActivityStatus();

    SalesByTypeResponse getSalesByType();

    List<TopActivityResponse> getTopActivities(String type, Integer limit);

    RegistrationGrowthResponse getRegistrations(String type, String periodType, Integer limit);

    List<VendorActivityResponse> getRecentVendorActivities(Integer limit);
}
