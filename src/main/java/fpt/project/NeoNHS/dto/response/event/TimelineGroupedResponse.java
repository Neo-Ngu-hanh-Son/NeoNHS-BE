package fpt.project.NeoNHS.dto.response.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineGroupedResponse {
    private LocalDate date;
    private String lunarDate;
    private String dayLabel;
    private List<EventTimelineResponse> timelines;
}
