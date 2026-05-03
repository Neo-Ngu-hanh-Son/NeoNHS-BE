package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.EventPointRequest;
import fpt.project.NeoNHS.dto.response.event.EventPointResponse;
import fpt.project.NeoNHS.entity.EventPoint;
import fpt.project.NeoNHS.entity.EventPointTag;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.EventPointRepository;
import fpt.project.NeoNHS.repository.EventPointTagRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.EventPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static fpt.project.NeoNHS.helpers.AuthHelper.getCurrentUserPrincipal;

@Service
@RequiredArgsConstructor
public class EventPointServiceImpl implements EventPointService {

    private final EventPointRepository eventPointRepository;
    private final EventPointTagRepository tagRepository;
    private final UserRepository userRepository;

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

        return EventPointResponse.fromEntity(eventPointRepository.save(point));
    }

    @Override
    @Transactional
    public EventPointResponse updatePoint(UUID id, EventPointRequest request) {
        EventPoint point = eventPointRepository.findById(id)
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
        if (request.getRestore() != null && request.getRestore() && point.getDeletedAt() != null) {
            point.setDeletedAt(null);
            point.setDeletedBy(null);
        }
        point.setEventPointTag(tag);

        return EventPointResponse.fromEntity(eventPointRepository.save(point));
    }

    @Override
    @Transactional(readOnly = true)
    public EventPointResponse getPointById(UUID id) {
        return eventPointRepository.findById(id)
                .map(EventPointResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("EventPoint not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventPointResponse> getAllPoints() {
        return eventPointRepository.findAll().stream()
                .filter(point -> point.getDeletedAt() == null)
                .map(EventPointResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventPointResponse> getAllPointsAdmin() {
        return eventPointRepository.findAll().stream()
                .map(EventPointResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventPointResponse> getPointsByTagId(UUID tagId) {
        return eventPointRepository.findByEventPointTagId(tagId).stream()
                .map(EventPointResponse::fromEntity)
                .toList();
    }

    /**
     * NOTE: SOFT DELETE ONLY
     *
     * @param id
     */
    @Override
    @Transactional
    public void deletePoint(UUID id) {
        var user = getCurrentUser();
        if (user.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("You do not have permission to delete this point");
        }
        EventPoint point = eventPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventPoint not found with id: " + id));
        point.setDeletedAt(LocalDateTime.now());
        point.setDeletedBy(user.getId());
        eventPointRepository.save(point);
    }

    @Override
    public void restorePoint(UUID id) {
        var evtPoint = eventPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventPoint not found with id: " + id));
        evtPoint.setDeletedAt(null);
        evtPoint.setDeletedBy(null);
        eventPointRepository.save(evtPoint);
    }

    private User getCurrentUser() {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
