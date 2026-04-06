package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.EventPointRequest;
import fpt.project.NeoNHS.dto.response.event.EventPointResponse;
import fpt.project.NeoNHS.entity.EventPoint;
import fpt.project.NeoNHS.entity.EventPointTag;
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
        if (request.getEventPointTagId() != null) {
            tag = tagRepository.findById(request.getEventPointTagId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        }

        EventPoint point = EventPoint.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageList(request.getImageList())
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
                .orElseThrow(() -> new ResourceNotFoundException("Point not found"));

        EventPointTag tag = null;
        if (request.getEventPointTagId() != null) {
            tag = tagRepository.findById(request.getEventPointTagId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        }

        point.setName(request.getName());
        point.setDescription(request.getDescription());
        point.setImageList(request.getImageList());
        point.setLatitude(request.getLatitude());
        point.setLongitude(request.getLongitude());
        point.setAddress(request.getAddress());
        point.setEventPointTag(tag);

        return EventPointResponse.fromEntity(pointRepository.save(point));
    }

    @Override
    @Transactional(readOnly = true)
    public EventPointResponse getPointById(UUID id) {
        return pointRepository.findById(id)
                .map(EventPointResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Point not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventPointResponse> getAllPoints() {
        return pointRepository.findAll().stream()
                .map(EventPointResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deletePoint(UUID id) {
        pointRepository.deleteById(id);
    }
}
