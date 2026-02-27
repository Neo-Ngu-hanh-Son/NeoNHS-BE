package fpt.project.NeoNHS.dto.request.admin;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardTimeFilterRequest {

    /**
     * WEEKLY | MONTHLY
     */
    private String periodType;

    /**
     * Số tuần / tháng gần nhất
     */
    private Integer limit;
}
