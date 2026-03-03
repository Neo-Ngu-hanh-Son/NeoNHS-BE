package fpt.project.NeoNHS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
  private long blogCount;
  private long workshopCount;
  private long eventCount;
  private long attractionCount;
  private long pointCount;
}
