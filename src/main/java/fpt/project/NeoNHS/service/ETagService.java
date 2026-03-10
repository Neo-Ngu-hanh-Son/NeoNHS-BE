package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.event.CreateTagRequest;
import fpt.project.NeoNHS.dto.request.event.UpdateTagRequest;
import fpt.project.NeoNHS.dto.response.event.TagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ETagService {

    TagResponse createTag(CreateTagRequest request);

    TagResponse updateTag(UUID id, UpdateTagRequest request);

    Page<TagResponse> getAllTags(Pageable pageable);

    List<TagResponse> getAllActiveTags();

    TagResponse getTagById(UUID id);

    void softDeleteTag(UUID id, UUID deletedBy);

    TagResponse restoreTag(UUID id);
}
