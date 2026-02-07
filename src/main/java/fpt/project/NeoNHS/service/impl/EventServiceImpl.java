package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.CreateEventRequest;
import fpt.project.NeoNHS.dto.request.event.EventFilterRequest;
import fpt.project.NeoNHS.dto.request.event.UpdateEventRequest;
import fpt.project.NeoNHS.dto.response.event.EventResponse;
import fpt.project.NeoNHS.entity.ETag;
import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.entity.EventTag;
import fpt.project.NeoNHS.entity.EventTagId;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.ETagRepository;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.EventTagRepository;
import fpt.project.NeoNHS.service.EventService;
import fpt.project.NeoNHS.specification.EventSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ETagRepository eTagRepository;
    private final EventTagRepository eventTagRepository;

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        Event event = Event.builder()
                .name(request.getName())
                .shortDescription(request.getShortDescription())
                .fullDescription(request.getFullDescription())
                .locationName(request.getLocationName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isTicketRequired(request.getIsTicketRequired() != null ? request.getIsTicketRequired() : false)
                .price(request.getPrice())
                .maxParticipants(request.getMaxParticipants())
                .build();

        Event savedEvent = eventRepository.save(event);

        // Assign tags if provided
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<EventTag> eventTags = createEventTags(savedEvent, request.getTagIds());
            savedEvent.setEventTags(eventTags);
        }

        return EventResponse.fromEntity(savedEvent);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (request.getName() != null) {
            event.setName(request.getName());
        }
        if (request.getShortDescription() != null) {
            event.setShortDescription(request.getShortDescription());
        }
        if (request.getFullDescription() != null) {
            event.setFullDescription(request.getFullDescription());
        }
        if (request.getLocationName() != null) {
            event.setLocationName(request.getLocationName());
        }
        if (request.getLatitude() != null) {
            event.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            event.setLongitude(request.getLongitude());
        }
        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }
        if (request.getIsTicketRequired() != null) {
            event.setIsTicketRequired(request.getIsTicketRequired());
        }
        if (request.getPrice() != null) {
            event.setPrice(request.getPrice());
        }
        if (request.getMaxParticipants() != null) {
            event.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }

        // Validate time consistency after updates
        if (event.getEndTime().isBefore(event.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Update tags if provided
        if (request.getTagIds() != null) {
            eventTagRepository.deleteByEventId(id);
            if (!request.getTagIds().isEmpty()) {
                List<EventTag> eventTags = createEventTags(event, request.getTagIds());
                event.setEventTags(eventTags);
            } else {
                event.setEventTags(new ArrayList<>());
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return EventResponse.fromEntity(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(EventFilterRequest filter, Pageable pageable) {
        return eventRepository.findAll(EventSpecification.withFilters(filter), pageable)
                .map(EventResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents(EventFilterRequest filter) {
        return eventRepository.findAll(EventSpecification.withFilters(filter))
                .stream()
                .map(EventResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return EventResponse.fromEntity(event);
    }

    private List<EventTag> createEventTags(Event event, List<UUID> tagIds) {
        List<EventTag> eventTags = new ArrayList<>();

        for (UUID tagId : tagIds) {
            ETag eTag = eTagRepository.findById(tagId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

            EventTagId eventTagId = new EventTagId(event.getId(), eTag.getId());
            EventTag eventTag = EventTag.builder()
                    .id(eventTagId)
                    .event(event)
                    .eTag(eTag)
                    .build();

            eventTags.add(eventTagRepository.save(eventTag));
        }

        return eventTags;
    }
}
