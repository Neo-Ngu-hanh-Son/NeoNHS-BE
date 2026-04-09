package fpt.project.NeoNHS.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import fpt.project.NeoNHS.dto.request.point.CheckinPointRequest;
import fpt.project.NeoNHS.dto.response.point.CheckinPointResponse;

public interface CheckinPointService {

    CheckinPointResponse getCheckinPointById(UUID pointId, UUID checkinId);

    Page<CheckinPointResponse> getAllCheckinFromPointId(UUID pointId, int page, int size, String sortBy, String sortDir,
                                                        String search);

    List<CheckinPointResponse> getAllCheckinPoints();

    CheckinPointResponse createCheckinPoint(CheckinPointRequest request);

    CheckinPointResponse updateCheckinPoint(UUID id, CheckinPointRequest request);

    void deleteCheckinPoint(UUID id, UUID currentUserId);

    void restoreCheckinPoint(UUID id, UUID currentUserId);

    CheckinPointResponse getCheckinPointByIdForAdmin(UUID id);

    Page<CheckinPointResponse> getAllCheckinPointsForAdmin(int page, int size, String sortBy, String sortDir, String search, boolean includeDeleted);

    List<CheckinPointResponse> getNearbyCheckinPoints(double latitude, double longitude, double radius);
}
