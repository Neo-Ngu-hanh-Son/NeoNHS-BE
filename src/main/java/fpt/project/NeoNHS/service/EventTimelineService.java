package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.event.EventTimelineRequest;
import fpt.project.NeoNHS.dto.response.event.EventPointResponse;
import fpt.project.NeoNHS.dto.response.event.EventPointTagResponse;
import fpt.project.NeoNHS.dto.response.event.EventTimelineResponse;
import fpt.project.NeoNHS.dto.response.event.TimelineGroupedResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventTimelineService {
    EventTimelineResponse createTimeline(UUID eventId, EventTimelineRequest request);

    EventTimelineResponse updateTimeline(UUID eventId, UUID id, EventTimelineRequest request);

    EventTimelineResponse getTimelineById(UUID id);

    List<EventTimelineResponse> getTimelinesByEventId(UUID eventId);

    List<EventTimelineResponse> getTimelinesByEventAndDate(UUID eventId, LocalDate date);

    List<TimelineGroupedResponse> getTimelinesByEventGroupedByDate(UUID eventId);

    List<EventTimelineResponse> getAllTimelines();

    void deleteTimeline(UUID id);

    List<EventPointResponse> getAllEventPointByEventId(UUID eventId);

    List<EventPointTagResponse> getAllEventPointTagByEventId(UUID eventId);
}
