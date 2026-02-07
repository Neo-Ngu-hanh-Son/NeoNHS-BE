package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.event.CreateEventRequest;
import fpt.project.NeoNHS.dto.request.event.EventFilterRequest;
import fpt.project.NeoNHS.dto.request.event.UpdateEventRequest;
import fpt.project.NeoNHS.dto.response.event.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EventService {

    EventResponse createEvent(CreateEventRequest request);

    EventResponse updateEvent(UUID id, UpdateEventRequest request);

    Page<EventResponse> getAllEvents(EventFilterRequest filter, Pageable pageable);

    List<EventResponse> getAllEvents(EventFilterRequest filter);

    EventResponse getEventById(UUID id);
}
