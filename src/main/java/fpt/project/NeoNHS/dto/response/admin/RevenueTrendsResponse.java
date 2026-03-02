package fpt.project.NeoNHS.dto.response.admin;

import lombok.*;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevenueTrendsResponse {
    private List<RevenueTrendItem> monthly;
    private List<RevenueTrendItem> weekly;
}
