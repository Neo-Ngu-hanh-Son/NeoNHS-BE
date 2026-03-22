package fpt.project.NeoNHS.dto.response.vendor.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorStatsResponse {
    private VendorStatCard revenue;
    private VendorStatCard workshops;
    private VendorStatCard bookings;
    private VendorStatCard vouchers;
}
