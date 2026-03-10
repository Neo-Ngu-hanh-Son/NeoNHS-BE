package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.event.CreateTagRequest;
import fpt.project.NeoNHS.dto.request.event.UpdateTagRequest;
import fpt.project.NeoNHS.dto.response.event.TagResponse;
import fpt.project.NeoNHS.entity.ETag;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.ETagRepository;
import fpt.project.NeoNHS.service.ETagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ETagServiceImpl implements ETagService {

    private final ETagRepository eTagRepository;

    @Override
    @Transactional
    public TagResponse createTag(CreateTagRequest request) {
        if (eTagRepository.existsByName(request.getName())) {
            throw new BadRequestException("Tag with name '" + request.getName() + "' already exists");
        }

        ETag eTag = ETag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .tagColor(request.getTagColor())
                .iconUrl(request.getIconUrl())
                .build();

        ETag saved = eTagRepository.save(eTag);
        return TagResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public TagResponse updateTag(UUID id, UpdateTagRequest request) {
        ETag eTag = eTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (request.getName() != null) {
            // Check duplicate name only if name is changing
            if (!eTag.getName().equals(request.getName()) && eTagRepository.existsByName(request.getName())) {
                throw new BadRequestException("Tag with name '" + request.getName() + "' already exists");
            }
            eTag.setName(request.getName());
        }
        if (request.getDescription() != null) {
            eTag.setDescription(request.getDescription());
        }
        if (request.getTagColor() != null) {
            eTag.setTagColor(request.getTagColor());
        }
        if (request.getIconUrl() != null) {
            eTag.setIconUrl(request.getIconUrl());
        }

        ETag updated = eTagRepository.save(eTag);
        return TagResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> getAllTags(Pageable pageable) {
        return eTagRepository.findAll(pageable)
                .map(TagResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAllActiveTags() {
        return eTagRepository.findAllByDeletedAtIsNull().stream()
                .map(TagResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getTagById(UUID id) {
        ETag eTag = eTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        return TagResponse.fromEntity(eTag);
    }

    @Override
    @Transactional
    public void softDeleteTag(UUID id, UUID deletedBy) {
        ETag eTag = eTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (eTag.getDeletedAt() != null) {
            throw new BadRequestException("Tag is already deleted");
        }

        eTag.setDeletedAt(LocalDateTime.now());
        eTag.setDeletedBy(deletedBy);
        eTagRepository.save(eTag);
    }

    @Override
    @Transactional
    public TagResponse restoreTag(UUID id) {
        ETag eTag = eTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (eTag.getDeletedAt() == null) {
            throw new BadRequestException("Tag is not deleted");
        }

        eTag.setDeletedAt(null);
        eTag.setDeletedBy(null);
        ETag restored = eTagRepository.save(eTag);
        return TagResponse.fromEntity(restored);
    }
}
