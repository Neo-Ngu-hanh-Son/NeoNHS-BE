package fpt.project.NeoNHS.dto.response.vendor.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRevenueSeriesResponse {
    private String range;
    private List<VendorRevenuePoint> points;
}
