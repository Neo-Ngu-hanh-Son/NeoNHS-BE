package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.event.EventTimelineRequest;
import fpt.project.NeoNHS.dto.response.event.EventTimelineResponse;

import java.util.List;
import java.util.UUID;

public interface EventTimelineService {
    EventTimelineResponse createTimeline(EventTimelineRequest request);
    EventTimelineResponse updateTimeline(UUID id, EventTimelineRequest request);
    EventTimelineResponse getTimelineById(UUID id);
    List<EventTimelineResponse> getTimelinesByEventId(UUID eventId);
    List<EventTimelineResponse> getAllTimelines();
    void deleteTimeline(UUID id);
}
