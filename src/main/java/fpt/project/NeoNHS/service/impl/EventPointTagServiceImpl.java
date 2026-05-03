package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.TimezoneConstants;
import fpt.project.NeoNHS.dto.request.event.EventPointTagRequest;
import fpt.project.NeoNHS.dto.response.event.EventPointTagResponse;
import fpt.project.NeoNHS.entity.EventPointTag;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.EventPointTagRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.EventPointTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static fpt.project.NeoNHS.helpers.AuthHelper.getCurrentUserPrincipal;

@Service
@RequiredArgsConstructor
public class EventPointTagServiceImpl implements EventPointTagService {

    private final EventPointTagRepository tagRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EventPointTagResponse createTag(EventPointTagRequest request) {
        EventPointTag tag = EventPointTag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .tagColor(request.getTagColor())
                .iconUrl(request.getIconUrl())
                .build();
        return EventPointTagResponse.fromEntity(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public EventPointTagResponse updateTag(UUID id, EventPointTagRequest request) {
        EventPointTag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventPointTag not found with id: " + id));

        if (request.getName() != null) tag.setName(request.getName());
        if (request.getDescription() != null) tag.setDescription(request.getDescription());
        if (request.getTagColor() != null) tag.setTagColor(request.getTagColor());
        if (request.getIconUrl() != null) tag.setIconUrl(request.getIconUrl());
        if (request.getRestore() != null && request.getRestore() && tag.getDeletedAt() != null) {
            tag.setDeletedAt(null);
            tag.setDeletedBy(null);
        }
        return EventPointTagResponse.fromEntity(tagRepository.save(tag));
    }

    @Override
    @Transactional(readOnly = true)
    public EventPointTagResponse getTagById(UUID id) {
        return tagRepository.findById(id)
                .map(EventPointTagResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("EventPointTag not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventPointTagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .filter(tag -> tag.getDeletedAt() == null)
                .map(EventPointTagResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventPointTagResponse> getAllTagsAdmin() {
        return tagRepository.findAll().stream()
                .map(EventPointTagResponse::fromEntity)
                .toList();
    }


    // SOFT DELETE ONLY
    @Override
    @Transactional
    public void deleteTag(UUID id) {
        var user = getCurrentUser();
        EventPointTag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventPointTag not found with id: " + id));
        tag.setDeletedAt(LocalDateTime.now(ZoneId.of(TimezoneConstants.ASIA_HO_CHI_MINH)));
        tag.setDeletedBy(user.getId());
        tagRepository.save(tag);
    }

    @Override
    public void restoreTag(UUID id) {
        var eventPointTag = tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("EventPointTag not found with id: " + id));
        eventPointTag.setDeletedAt(null);
        eventPointTag.setDeletedBy(null);
        tagRepository.save(eventPointTag);
    }

    private User getCurrentUser() {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
