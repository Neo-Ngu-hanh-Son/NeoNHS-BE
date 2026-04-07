package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.EventTimelineRequest;
import fpt.project.NeoNHS.dto.response.event.EventTimelineResponse;
import fpt.project.NeoNHS.dto.response.event.TimelineGroupedResponse;
import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.entity.EventPoint;
import fpt.project.NeoNHS.entity.EventTimeline;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.helpers.LunarDateUtil;
import fpt.project.NeoNHS.repository.EventPointRepository;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.EventTimelineRepository;
import fpt.project.NeoNHS.service.EventTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventTimelineServiceImpl implements EventTimelineService {

    private final EventTimelineRepository timelineRepository;
    private final EventPointRepository pointRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public EventTimelineResponse createTimeline(UUID eventId, EventTimelineRequest request) {
        // TODO: create time line with point will be modify here
        EventPoint point = null;
        if (request.getEventPointId() != null) {
            point = pointRepository.findById(request.getEventPointId())
                    .orElseThrow(() -> new ResourceNotFoundException("Point not found"));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        validateTimelineTime(request, event);

        String lunarDate = LunarDateUtil.convertSolarToLunar(request.getDate());

        EventTimeline timeline = EventTimeline.builder()
                .name(request.getName())
                .description(request.getDescription())
                .organizer(request.getOrganizer())
                .coOrganizer(request.getCoOrganizer())
                .date(request.getDate())
                .lunarDate(lunarDate)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .eventPoint(point)
                .event(event)
                .build();

        return EventTimelineResponse.fromEntity(timelineRepository.save(timeline));
    }

    @Override
    @Transactional
    public EventTimelineResponse updateTimeline(UUID eventId, UUID id, EventTimelineRequest request) {
        EventTimeline timeline = timelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timeline not found"));

        EventPoint point = null;
        if (request.getEventPointId() != null) {
            point = pointRepository.findById(request.getEventPointId())
                    .orElseThrow(() -> new ResourceNotFoundException("Point not found"));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        validateTimelineTime(request, event);

        String lunarDate = LunarDateUtil.convertSolarToLunar(request.getDate());

        timeline.setName(request.getName());
        timeline.setDescription(request.getDescription());
        timeline.setOrganizer(request.getOrganizer());
        timeline.setCoOrganizer(request.getCoOrganizer());
        timeline.setDate(request.getDate());
        timeline.setLunarDate(lunarDate);
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
    @Transactional(readOnly = true)
    public List<EventTimelineResponse> getTimelinesByEventAndDate(UUID eventId, LocalDate date) {
        return timelineRepository.findByEventIdAndDate(eventId, date).stream()
                .map(EventTimelineResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineGroupedResponse> getTimelinesByEventGroupedByDate(UUID eventId) {
        List<EventTimeline> timelines = timelineRepository.findByEventIdOrderByDateAscStartTimeAsc(eventId);

        Map<LocalDate, List<EventTimeline>> grouped = new java.util.LinkedHashMap<>();
        for (EventTimeline timeline : timelines) {
            grouped.computeIfAbsent(timeline.getDate(), k -> new java.util.ArrayList<>()).add(timeline);
        }

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<EventTimeline> dayTimelines = entry.getValue();
                    String lunarDate = dayTimelines.isEmpty() ? null : dayTimelines.get(0).getLunarDate();

                    return TimelineGroupedResponse.builder()
                            .date(entry.getKey())
                            .lunarDate(lunarDate)
                            .timelines(dayTimelines.stream()
                                    .map(EventTimelineResponse::fromEntity)
                                    .toList())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public void deleteTimeline(UUID id) {
        timelineRepository.deleteById(id);
    }

    private void validateTimelineTime(EventTimelineRequest request, Event event) {
        LocalDateTime timelineStart = LocalDateTime.of(request.getDate(), request.getStartTime());
        LocalDateTime timelineEnd = LocalDateTime.of(request.getDate(), request.getEndTime());

        if (timelineStart.isAfter(timelineEnd)) {
            throw new BadRequestException("Timeline start time must be before end time");
        }

        if (timelineStart.isBefore(event.getStartTime()) || timelineEnd.isAfter(event.getEndTime())) {
            throw new BadRequestException("Timeline must be within the event's start and end time");
        }
    }
}
