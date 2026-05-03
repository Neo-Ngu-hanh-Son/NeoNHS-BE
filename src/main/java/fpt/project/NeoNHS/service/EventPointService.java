package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.event.EventPointRequest;
import fpt.project.NeoNHS.dto.response.event.EventPointResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface EventPointService {
    EventPointResponse createPoint(EventPointRequest request);
    EventPointResponse updatePoint(UUID id, EventPointRequest request);
    EventPointResponse getPointById(UUID id);
    List<EventPointResponse> getAllPoints();

    @Transactional(readOnly = true)
    List<EventPointResponse> getAllPointsAdmin();

    List<EventPointResponse> getPointsByTagId(UUID tagId);
    void deletePoint(UUID id);

    void restorePoint(UUID id);
}
