package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.workshop.CreateWTagRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWTagRequest;
import fpt.project.NeoNHS.dto.response.workshop.WTagResponse;
import fpt.project.NeoNHS.entity.WTag;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.WTagRepository;
import fpt.project.NeoNHS.service.WTagService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WTagServiceImpl implements WTagService {

    private final WTagRepository wTagRepository;

    @Override
    public WTagResponse createWTag(CreateWTagRequest request) {
        WTag wTag = WTag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .tagColor(request.getTagColor())
                .iconUrl(request.getIconUrl())
                .build();

        WTag savedTag = wTagRepository.save(wTag);
        return mapToResponse(savedTag);
    }

    @Override
    public WTagResponse getWTagById(UUID id) {
        WTag wTag = wTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WTag", "id", id));
        return mapToResponse(wTag);
    }

    @Override
    public List<WTagResponse> getAllWTags() {
        return wTagRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WTagResponse updateWTag(UUID id, UpdateWTagRequest request) {
        WTag wTag = wTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WTag", "id", id));

        if (request.getName() != null) {
            wTag.setName(request.getName());
        }
        if (request.getDescription() != null) {
            wTag.setDescription(request.getDescription());
        }
        if (request.getTagColor() != null) {
            wTag.setTagColor(request.getTagColor());
        }
        if (request.getIconUrl() != null) {
            wTag.setIconUrl(request.getIconUrl());
        }

        WTag updatedTag = wTagRepository.save(wTag);
        return mapToResponse(updatedTag);
    }

    @Override
    public void deleteWTag(UUID id) {
        WTag wTag = wTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WTag", "id", id));
        wTagRepository.delete(wTag);
    }

    private WTagResponse mapToResponse(WTag wTag) {
        return WTagResponse.builder()
                .id(wTag.getId())
                .name(wTag.getName())
                .description(wTag.getDescription())
                .tagColor(wTag.getTagColor())
                .iconUrl(wTag.getIconUrl())
                .build();
    }
}
