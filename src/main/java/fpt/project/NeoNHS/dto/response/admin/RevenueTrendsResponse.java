package fpt.project.NeoNHS.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevenueTrendsResponse {

    private Summary summary;
    private List<TrendPoint> trends;
    private Metadata metadata;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Summary {
        private BigDecimal currentTotal;
        private BigDecimal previousTotal;
        private Double growthRate;
        private BigDecimal averageValue;
        private BigDecimal peakValue;
        private String peakPeriod;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TrendPoint {
        private String periodKey;
        private String period;
        private BigDecimal revenue;
        private BigDecimal previousRevenue;
        private long transactionCount;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Metadata {
        private String currency;
        private String periodType;
        private int pointCount;
    }
}

