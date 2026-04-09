package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.EventPointRequest;
import fpt.project.NeoNHS.dto.request.event.EventTimelineRequest;
import fpt.project.NeoNHS.dto.response.event.EventTimelineResponse;
import fpt.project.NeoNHS.dto.response.event.TimelineGroupedResponse;
import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.entity.EventPoint;
import fpt.project.NeoNHS.entity.EventPointTag;
import fpt.project.NeoNHS.entity.EventTimeline;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.helpers.LunarDateUtil;
import fpt.project.NeoNHS.repository.EventPointRepository;
import fpt.project.NeoNHS.repository.EventPointTagRepository;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.EventTimelineRepository;
import fpt.project.NeoNHS.service.EventTimelineService;
import fpt.project.NeoNHS.service.ImageUploadService;
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
    private final EventPointTagRepository pointTagRepository;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public EventTimelineResponse createTimeline(UUID eventId, EventTimelineRequest request) {
        if (request.getEventPoint() == null) {
            throw new BadRequestException("Event point cannot be null");
        }

        if (request.getEventPoint().getEventPointTagRequest() == null) {
            throw new BadRequestException("Event point tag request cannot be null");
        }

        var requestPointTag = request.getEventPoint().getEventPointTagRequest();
        EventPointTag eventPointTag = EventPointTag.builder()
                .name(requestPointTag.getName())
                .description(requestPointTag.getDescription())
                .iconUrl(requestPointTag.getIconUrl())
                .tagColor(requestPointTag.getTagColor())
                .build();
        var saveEventPointTag = pointTagRepository.save(eventPointTag);

        EventPoint point = EventPoint.builder()
                .latitude(request.getEventPoint().getLatitude())
                .longitude(request.getEventPoint().getLongitude())
                .name(request.getEventPoint().getName())
                .description(request.getEventPoint().getDescription())
                .address(request.getEventPoint().getAddress())
                .eventPointTag(saveEventPointTag)
                .imageList(request.getEventPoint().getImageUrl())
                .build();
        var savedPoint = pointRepository.save(point);

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
                .eventPoint(savedPoint)
                .event(event)
                .build();

        return EventTimelineResponse.fromEntity(timelineRepository.save(timeline));
    }

    @Override
    @Transactional
    public EventTimelineResponse updateTimeline(UUID eventId, UUID id, EventTimelineRequest request) {
        EventTimeline timeline = timelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timeline not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        validateTimelineTime(request, event);

        EventPoint point = null;

        if (request.getEventPoint() != null) {
            var pointRequest = request.getEventPoint();
            EventPointTag tag = resolvePointTag(pointRequest, timeline);
            point = resolvePoint(pointRequest, timeline, tag);

        } else if (request.getEventPointId() != null) {
            point = pointRepository.findById(request.getEventPointId())
                    .orElseThrow(() -> new ResourceNotFoundException("Point not found"));
        }
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

    /**
     * Resolves the EventPointTag for an update operation.
     * If the timeline's existing point already has a tag, it updates that tag
     * in-place.
     * Otherwise, it creates a new tag from the request.
     */
    private EventPointTag resolvePointTag(EventPointRequest pointRequest,
            EventTimeline timeline) {
        var tagRequest = pointRequest.getEventPointTagRequest();

        EventPointTag tag = (timeline.getEventPoint() != null && timeline.getEventPoint().getEventPointTag() != null)
                ? timeline.getEventPoint().getEventPointTag()
                : new EventPointTag();

        if (tagRequest != null) {
            tag.setName(tagRequest.getName());
            tag.setDescription(tagRequest.getDescription());
            tag.setTagColor(tagRequest.getTagColor());
            tag.setIconUrl(tagRequest.getIconUrl());
        } else if (pointRequest.getEventPointTagId() != null) {
            return pointTagRepository.findById(pointRequest.getEventPointTagId())
                    .orElseThrow(() -> new ResourceNotFoundException("Point tag not found"));
        }

        return pointTagRepository.save(tag);
    }

    /**
     * Resolves the EventPoint for an update operation.
     * If the timeline already has a linked point, it updates that point in-place.
     * Otherwise, it creates a new point from the request.
     */
    private EventPoint resolvePoint(EventPointRequest pointRequest,
            EventTimeline timeline,
            EventPointTag tag) {
        EventPoint point = (timeline.getEventPoint() != null)
                ? timeline.getEventPoint()
                : new EventPoint();

        point.setName(pointRequest.getName());
        point.setDescription(pointRequest.getDescription());
        point.setImageList(pointRequest.getImageUrl());
        point.setLatitude(pointRequest.getLatitude());
        point.setLongitude(pointRequest.getLongitude());
        point.setAddress(pointRequest.getAddress());
        point.setEventPointTag(tag);

        return pointRepository.save(point);
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
