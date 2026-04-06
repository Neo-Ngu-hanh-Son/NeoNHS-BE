package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.EventPointTagRequest;
import fpt.project.NeoNHS.dto.response.event.EventPointTagResponse;
import fpt.project.NeoNHS.entity.EventPointTag;
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
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        tag.setTagColor(request.getTagColor());
        tag.setIconUrl(request.getIconUrl());
        return EventPointTagResponse.fromEntity(tagRepository.save(tag));
    }

    @Override
    @Transactional(readOnly = true)
    public EventPointTagResponse getTagById(UUID id) {
        return tagRepository.findById(id)
                .map(EventPointTagResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventPointTagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(EventPointTagResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteTag(UUID id) {
        tagRepository.deleteById(id);
    }
}
