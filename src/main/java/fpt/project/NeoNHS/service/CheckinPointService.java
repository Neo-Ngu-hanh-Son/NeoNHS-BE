package fpt.project.NeoNHS.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import fpt.project.NeoNHS.dto.request.point.CheckinPointRequest;
import fpt.project.NeoNHS.dto.response.point.PointCheckinResponse;

public interface CheckinPointService {

    PointCheckinResponse getCheckinPointById(UUID pointId, UUID checkinId);

    Page<PointCheckinResponse> getAllCheckinFromPointId(UUID pointId, int page, int size, String sortBy, String sortDir,
                                                        String search);

    List<PointCheckinResponse> getAllCheckinPoints();

    PointCheckinResponse createCheckinPoint(CheckinPointRequest request);

    PointCheckinResponse updateCheckinPoint(UUID id, CheckinPointRequest request);

    void deleteCheckinPoint(UUID id, UUID currentUserId);

    PointCheckinResponse getCheckinPointByIdForAdmin(UUID id);

    Page<PointCheckinResponse> getAllCheckinPointsForAdmin(int page, int size, String sortBy, String sortDir, String search, boolean includeDeleted);

    List<PointCheckinResponse> getNearbyCheckinPoints(double latitude, double longitude, double radius);
}
