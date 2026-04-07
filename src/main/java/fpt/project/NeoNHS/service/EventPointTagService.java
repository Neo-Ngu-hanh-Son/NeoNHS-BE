package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.event.EventPointTagRequest;
import fpt.project.NeoNHS.dto.response.event.EventPointTagResponse;

import java.util.List;
import java.util.UUID;

public interface EventPointTagService {
    EventPointTagResponse createTag(EventPointTagRequest request);
    EventPointTagResponse updateTag(UUID id, EventPointTagRequest request);
    EventPointTagResponse getTagById(UUID id);
    List<EventPointTagResponse> getAllTags();
    void deleteTag(UUID id);
}
