package fpt.project.NeoNHS.dto.response.event;

import fpt.project.NeoNHS.entity.EventTimeline;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTimelineResponse {
    private UUID id;
    private String name;
    private String description;
    private String organizer;
    private String coOrganizer;
    private LocalDate date;
    private String lunarDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private EventPointResponse eventPoint;
    private UUID eventId;

    public static EventTimelineResponse fromEntity(EventTimeline timeline) {
        if (timeline == null) return null;
        return EventTimelineResponse.builder()
                .id(timeline.getId())
                .name(timeline.getName())
                .description(timeline.getDescription())
                .organizer(timeline.getOrganizer())
                .coOrganizer(timeline.getCoOrganizer())
                .date(timeline.getDate())
                .lunarDate(timeline.getLunarDate())
                .startTime(timeline.getStartTime())
                .endTime(timeline.getEndTime())
                .eventPoint(EventPointResponse.fromEntity(timeline.getEventPoint()))
                .eventId(timeline.getEvent() != null ? timeline.getEvent().getId() : null)
                .build();
    }
}
