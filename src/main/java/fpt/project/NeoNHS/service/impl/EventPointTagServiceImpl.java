package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.EventPointTagRequest;
import fpt.project.NeoNHS.dto.response.event.EventPointTagResponse;
import fpt.project.NeoNHS.entity.EventPointTag;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.EventPointTagRepository;
import fpt.project.NeoNHS.service.EventPointTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventPointTagServiceImpl implements EventPointTagService {

    private final EventPointTagRepository tagRepository;

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
                .map(EventPointTagResponse::fromEntity)
                .toList();
    }

    // NOTE: Because event point tag can be reused across many event and used by many event points.
    // When
    @Override
    @Transactional
    public void deleteTag(UUID id) {
        EventPointTag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventPointTag not found with id: " + id));
//        if (tag.getEventPoints() != null && !tag.getEventPoints().isEmpty()) {
//            throw new BadRequestException("Cannot delete tag that is being used by event points");
//        }
        tagRepository.delete(tag);
    }
}
