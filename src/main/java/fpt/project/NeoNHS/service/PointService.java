package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.point.PointRequest;
import fpt.project.NeoNHS.dto.response.point.PointResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PointService {
    PointResponse createPoint(PointRequest request);

    PointResponse updatePoint(UUID id, PointRequest request);

    void deletePoint(UUID id, UUID userId);

    PointResponse getPointById(UUID id);

    List<PointResponse> getPointsByAttraction(UUID attractionId);

    Page<PointResponse> getAllPointsWithPagination(UUID attractionId, int page, int size, String sortBy, String sortDir,
            String search);

    Page<PointResponse> getAllPoints(int page, int size, String sortBy, String sortDir, String search);
}
