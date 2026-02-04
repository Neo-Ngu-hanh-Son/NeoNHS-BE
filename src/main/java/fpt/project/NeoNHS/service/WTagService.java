package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.workshop.CreateWTagRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWTagRequest;
import fpt.project.NeoNHS.dto.response.workshop.WTagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface WTagService {

    WTagResponse createWTag(CreateWTagRequest request);

    WTagResponse getWTagById(UUID id);

    List<WTagResponse> getAllWTags();

    Page<WTagResponse> getAllWTags(Pageable pageable);

    Page<WTagResponse> searchWTags(String keyword, String name, String tagColor, Pageable pageable);

    WTagResponse updateWTag(UUID id, UpdateWTagRequest request);

    void deleteWTag(UUID id);
}
