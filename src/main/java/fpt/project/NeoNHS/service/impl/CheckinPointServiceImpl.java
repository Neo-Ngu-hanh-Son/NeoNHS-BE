package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.point.CheckinPointRequest;
import fpt.project.NeoNHS.dto.response.point.CheckinPointResponse;
import fpt.project.NeoNHS.entity.CheckinPoint;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.helpers.AuthHelper;
import fpt.project.NeoNHS.repository.CheckinPointRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.service.CheckinPointService;
import fpt.project.NeoNHS.service.GeoService;
import fpt.project.NeoNHS.specification.CheckinPointSpecification;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckinPointServiceImpl implements CheckinPointService {

    private final CheckinPointRepository checkinPointRepository;
    private final PointRepository pointRepository; // needed to verify point existence
    private final GeoService geoService; // Geo and redis related operations

    @Override
    public CheckinPointResponse getCheckinPointById(UUID pointId, UUID checkinId) {
        if (!pointRepository.existsById(pointId)) {
            throw new ResourceNotFoundException("Point not found with id: " + pointId);
        }

        CheckinPoint checkinPoint = checkinPointRepository.findById(checkinId)
                .filter(cp -> cp.getDeletedAt() == null)
                .filter(cp -> cp.getPoint().getId().equals(pointId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CheckinPoint not found with id " + checkinId + " for point " + pointId));

        return CheckinPointResponse.fromEntity(checkinPoint, false);
    }

    // For now, the default sort by is by createdAt (you might want to sort by user
    // location instead).
    @Override
    public Page<CheckinPointResponse> getAllCheckinFromPointId(UUID pointId, int page, int size, String sortBy,
                                                               String sortDir,
                                                               String search) {
        if (!pointRepository.existsById(pointId)) {
            throw new ResourceNotFoundException("Point not found with id: " + pointId);
        }

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), sort);

        return checkinPointRepository.findAll(CheckinPointSpecification.withFilters(pointId, search, true), pageable)
                .map(p -> CheckinPointResponse.fromEntity(p, false));
    }

    @Override
    public List<CheckinPointResponse> getAllCheckinPoints() {
        List<CheckinPoint> checkinPoints = checkinPointRepository.findAll();
        return checkinPoints.stream()
                .map(p -> CheckinPointResponse.fromEntity(p, false)).toList();
    }

    @Override
    public CheckinPointResponse createCheckinPoint(CheckinPointRequest request) {
        Point point = pointRepository.findById(request.getPointId())
                .orElseThrow(() -> new ResourceNotFoundException("Point not found with id: " + request.getPointId()));

        if (request.getRewardPoints() < 10) {
            throw new BadRequestException("Reward Points must be greater than 10");
        }

        CheckinPoint checkinPoint = CheckinPoint.builder()
                .point(point)
                .name(request.getName())
                .description(request.getDescription())
                .position(request.getPosition())
                .thumbnailUrl(request.getThumbnailUrl() != null ? request.getThumbnailUrl() : point.getThumbnailUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .qrCode(request.getQrCode())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .rewardPoints(request.getRewardPoints())
                .build();

        CheckinPoint saved = checkinPointRepository.save(checkinPoint);
        // Sync
        geoService.addCheckinToRedis(saved.getId().toString(), saved.getLongitude().doubleValue(),
                saved.getLatitude().doubleValue());
        return CheckinPointResponse.fromEntity(saved, false);
    }

    @Override
    public CheckinPointResponse updateCheckinPoint(UUID id, CheckinPointRequest request) {
        CheckinPoint checkinPoint = checkinPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + id));

        if (request.getPointId() != null && !checkinPoint.getPoint().getId().equals(request.getPointId())) {
            Point point = pointRepository.findById(request.getPointId())
                    .orElseThrow(() -> new ResourceNotFoundException("Point not found with id: " + request.getPointId()));
            checkinPoint.setPoint(point);
        }

        if (request.getName() != null) checkinPoint.setName(request.getName());
        if (request.getDescription() != null) checkinPoint.setDescription(request.getDescription());
        if (request.getPosition() != null) checkinPoint.setPosition(request.getPosition());
        if (request.getThumbnailUrl() != null) checkinPoint.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getIsActive() != null) checkinPoint.setIsActive(request.getIsActive());
        if (request.getQrCode() != null) checkinPoint.setQrCode(request.getQrCode());
        if (request.getLongitude() != null) checkinPoint.setLongitude(request.getLongitude());
        if (request.getLatitude() != null) checkinPoint.setLatitude(request.getLatitude());
        if (request.getRewardPoints() != null) checkinPoint.setRewardPoints(request.getRewardPoints());

        CheckinPoint saved = checkinPointRepository.save(checkinPoint);
        if (request.getLatitude() != null || request.getLongitude() != null) {
            // Sync to redis if location changed
            geoService.updateCheckinInRedis(saved.getId().toString(), saved.getLongitude().doubleValue(),
                    saved.getLatitude().doubleValue());
        }
        return CheckinPointResponse.fromEntity(saved, false);
    }

    /**
     * Soft delete (NOTE: This only hide from user, while any user that already has this checkin point will not be affected
     * Therefore we don't need to check if the check-in point has user check-ins
     *
     * @param id
     * @param currentUserId
     */
    @Override
    public void deleteCheckinPoint(UUID id, UUID currentUserId) {
        CheckinPoint checkinPoint = checkinPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + id));
//        if (!checkinPoint.getUserCheckIns().isEmpty()) {
//            throw new BadRequestException("Cannot delete check-in point that has user check-ins");
//        }

        checkinPoint.setDeletedAt(LocalDateTime.now());
        checkinPoint.setDeletedBy(currentUserId);
        checkinPoint.setIsActive(false);
        checkinPointRepository.save(checkinPoint);

        // Sync
        geoService.removeCheckinFromRedis(id.toString());
    }

    @Override
    public void restoreCheckinPoint(UUID id, UUID currentUserId) {
        CheckinPoint checkinPoint = checkinPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + id));

        checkinPoint.setDeletedAt(null);
        checkinPoint.setDeletedBy(null);
        checkinPoint.setIsActive(true);
        checkinPointRepository.save(checkinPoint);
        // Sync
        geoService.addCheckinToRedis(checkinPoint.getId().toString(), checkinPoint.getLongitude().doubleValue(), checkinPoint.getLatitude().doubleValue());
    }

    @Override
    public CheckinPointResponse getCheckinPointByIdForAdmin(UUID id) {
        CheckinPoint checkinPoint = checkinPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + id));
        return CheckinPointResponse.fromEntity(checkinPoint, false);
    }

    @Override
    public Page<CheckinPointResponse> getAllCheckinPointsForAdmin(int page, int size, String sortBy, String sortDir,
                                                                  String search, boolean includeDeleted) {
        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), sort);

        // !includeDeleted because the filters is isActive
        return checkinPointRepository.findAll(CheckinPointSpecification.withFilters(null, search, !includeDeleted), pageable)
                .map(p -> CheckinPointResponse.fromEntity(p, false));
    }

    @Override
    public List<CheckinPointResponse> getNearbyCheckinPoints(double latitude, double longitude, double radiusMeters) {
        List<String> idList = geoService.getCheckinsInRadius(latitude, longitude, radiusMeters);
        // Get all the checkin point entities from the database and map them to responses
        List<UUID> uuidList = idList.stream().map(UUID::fromString).toList();
        List<CheckinPoint> checkinPoints = new ArrayList<>();
        var principal = AuthHelper.getCurrentUserPrincipalSilent();
        if (principal == null) {
            // Just get all
            checkinPoints = checkinPointRepository.findAllById(uuidList);
        } else {
            // Get all but only the point that user has not checked in yet.
            checkinPoints = checkinPointRepository.findAllCheckinPointThatUserNotCheckined(uuidList, principal.getId());
        }
        return checkinPoints.stream()
                .map(p -> CheckinPointResponse.fromEntity(p, false))
                .toList();
    }
}
