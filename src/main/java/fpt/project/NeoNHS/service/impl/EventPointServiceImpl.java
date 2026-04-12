package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.EventPointRequest;
import fpt.project.NeoNHS.dto.response.event.EventPointResponse;
import fpt.project.NeoNHS.entity.EventPoint;
import fpt.project.NeoNHS.entity.EventPointTag;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.EventPointRepository;
import fpt.project.NeoNHS.repository.EventPointTagRepository;
import fpt.project.NeoNHS.service.EventPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventPointServiceImpl implements EventPointService {

    private final EventPointRepository pointRepository;
    private final EventPointTagRepository tagRepository;

    @Override
    @Transactional
    public EventPointResponse createPoint(EventPointRequest request) {
        EventPointTag tag = null;
        if (request.getEventPointTagRequest() != null) {
            tag = EventPointTag.builder()
                    .name(request.getEventPointTagRequest().getName())
                    .description(request.getEventPointTagRequest().getDescription())
                    .tagColor(request.getEventPointTagRequest().getTagColor())
                    .iconUrl(request.getEventPointTagRequest().getIconUrl())
                    .build();
        } else if (request.getEventPointTagId() != null) {
            tag = tagRepository.findById(request.getEventPointTagId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventPointTag not found with id: " + request.getEventPointTagId()));
        }

        EventPoint point = EventPoint.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageList(request.getImageUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .eventPointTag(tag)
                .build();

        return EventPointResponse.fromEntity(pointRepository.save(point));
    }

    @Override
    @Transactional
    public EventPointResponse updatePoint(UUID id, EventPointRequest request) {
        EventPoint point = pointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventPoint not found with id: " + id));

        EventPointTag tag = null;
        if (request.getEventPointTagId() != null) {
            tag = tagRepository.findById(request.getEventPointTagId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventPointTag not found with id: " + request.getEventPointTagId()));
        }

        if (request.getName() != null) point.setName(request.getName());
        if (request.getDescription() != null) point.setDescription(request.getDescription());
        if (request.getImageUrl() != null) point.setImageList(request.getImageUrl());
        if (request.getLatitude() != null) point.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) point.setLongitude(request.getLongitude());
        if (request.getAddress() != null) point.setAddress(request.getAddress());
        point.setEventPointTag(tag);

        return EventPointResponse.fromEntity(pointRepository.save(point));
    }

    @Override
    @Transactional(readOnly = true)
    public EventPointResponse getPointById(UUID id) {
        return pointRepository.findById(id)
                .map(EventPointResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("EventPoint not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventPointResponse> getAllPoints() {
        return pointRepository.findAll().stream()
                .map(EventPointResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventPointResponse> getPointsByTagId(UUID tagId) {
        return pointRepository.findByEventPointTagId(tagId).stream()
                .map(EventPointResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deletePoint(UUID id) {
        EventPoint point = pointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventPoint not found with id: " + id));

        if (point.getEventTimelines() != null && !point.getEventTimelines().isEmpty()) {
            throw new BadRequestException("Cannot delete point that is being used by event timelines");
        }
        pointRepository.delete(point);
    }
}
