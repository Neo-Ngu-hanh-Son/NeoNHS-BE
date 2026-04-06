package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.EventTimelineRequest;
import fpt.project.NeoNHS.dto.response.event.EventTimelineResponse;
import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.entity.EventPoint;
import fpt.project.NeoNHS.entity.EventTimeline;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.EventPointRepository;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.EventTimelineRepository;
import fpt.project.NeoNHS.service.EventTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventTimelineServiceImpl implements EventTimelineService {

    private final EventTimelineRepository timelineRepository;
    private final EventPointRepository pointRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public EventTimelineResponse createTimeline(EventTimelineRequest request) {
        EventPoint point = pointRepository.findById(request.getEventPointId())
                .orElseThrow(() -> new ResourceNotFoundException("Point not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        EventTimeline timeline = EventTimeline.builder()
                .name(request.getName())
                .description(request.getDescription())
                .organizer(request.getOrganizer())
                .coOrganizer(request.getCoOrganizer())
                .date(request.getDate())
                .lunarDate(request.getLunarDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .eventPoint(point)
                .event(event)
                .build();

        return EventTimelineResponse.fromEntity(timelineRepository.save(timeline));
    }

    @Override
    @Transactional
    public EventTimelineResponse updateTimeline(UUID id, EventTimelineRequest request) {
        EventTimeline timeline = timelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timeline not found"));

        EventPoint point = pointRepository.findById(request.getEventPointId())
                .orElseThrow(() -> new ResourceNotFoundException("Point not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        timeline.setName(request.getName());
        timeline.setDescription(request.getDescription());
        timeline.setOrganizer(request.getOrganizer());
        timeline.setCoOrganizer(request.getCoOrganizer());
        timeline.setDate(request.getDate());
        timeline.setLunarDate(request.getLunarDate());
        timeline.setStartTime(request.getStartTime());
        timeline.setEndTime(request.getEndTime());
        timeline.setEventPoint(point);
        timeline.setEvent(event);

        return EventTimelineResponse.fromEntity(timelineRepository.save(timeline));
    }

    @Override
    @Transactional(readOnly = true)
    public EventTimelineResponse getTimelineById(UUID id) {
        return timelineRepository.findById(id)
                .map(EventTimelineResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Timeline not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTimelineResponse> getTimelinesByEventId(UUID eventId) {
        return timelineRepository.findByEventId(eventId).stream()
                .map(EventTimelineResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTimelineResponse> getAllTimelines() {
        return timelineRepository.findAll().stream()
                .map(EventTimelineResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteTimeline(UUID id) {
        timelineRepository.deleteById(id);
    }
}
