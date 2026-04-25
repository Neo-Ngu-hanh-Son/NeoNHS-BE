package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.point.PointRequest;
import fpt.project.NeoNHS.dto.response.point.MapPointResponse;
import fpt.project.NeoNHS.dto.response.point.PointPanoramaResponse;
import fpt.project.NeoNHS.dto.response.point.PointResponse;
import fpt.project.NeoNHS.dto.response.review.ReviewImageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

public interface PointService {
    PointResponse createPoint(PointRequest request);

    PointResponse updatePoint(UUID id, PointRequest request);

    void deletePoint(UUID id, UUID userId);

    void hardDeletePoint(UUID id);

    void restorePoint(UUID id);

    PointResponse getPointById(UUID id);

    PointResponse getPointByIdForAdmin(UUID id);

    List<PointResponse> getPointsByAttraction(UUID attractionId);

    Page<PointResponse> getAllPointsWithPagination(UUID attractionId, int page, int size, String sortBy, String sortDir,
            String search);

    Page<PointResponse> getAllPointsWithPaginationForAdmin(UUID attractionId, int page, int size, String sortBy,
            String sortDir,
            String search, boolean includeDeleted);

    Page<PointResponse> getAllPoints(int page, int size, String sortBy, String sortDir, String search);

    List<MapPointResponse> getAllPointsOnMap();

    Page<PointResponse> getAllPointsForAdmin(int page, int size, String sortBy, String sortDir, String search,
            boolean includeDeleted);

    Page<ReviewImageResponse> getPointPublicCheckinImage(UUID pointId, PageRequest pageable);
}
