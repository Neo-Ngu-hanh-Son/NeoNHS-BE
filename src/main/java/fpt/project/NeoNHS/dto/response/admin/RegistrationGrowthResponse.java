package fpt.project.NeoNHS.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationGrowthResponse {

    private Summary summary;
    private List<TrendPoint> trends;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Summary {
        private long totalJoined;
        private long previousTotal;
        private Double growthRate;
        private Double activePercentage;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TrendPoint {
        private String periodKey;
        private String period;
        private long count;
        private long previousCount;
        private Breakdown breakdown;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Breakdown {
        private long individual;
        private long organization;
    }
}

