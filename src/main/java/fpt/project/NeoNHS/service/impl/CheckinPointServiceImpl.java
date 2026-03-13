package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.point.CheckinPointRequest;
import fpt.project.NeoNHS.dto.response.point.PointCheckinResponse;
import fpt.project.NeoNHS.entity.CheckinPoint;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.CheckinPointRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.service.CheckinPointService;
import fpt.project.NeoNHS.service.GeoService;
import fpt.project.NeoNHS.specification.CheckinPointSpecification;
import lombok.RequiredArgsConstructor;

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
    public PointCheckinResponse getCheckinPointById(UUID pointId, UUID checkinId) {
        if (!pointRepository.existsById(pointId)) {
            throw new ResourceNotFoundException("Point not found with id: " + pointId);
        }

        CheckinPoint checkinPoint = checkinPointRepository.findById(checkinId)
                .filter(cp -> cp.getDeletedAt() == null)
                .filter(cp -> cp.getPoint().getId().equals(pointId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CheckinPoint not found with id " + checkinId + " for point " + pointId));

        return mapToResponse(checkinPoint);
    }

    // For now, the default sort by is by createdAt (you might want to sort by user
    // location instead).
    @Override
    public Page<PointCheckinResponse> getAllCheckinFromPointId(UUID pointId, int page, int size, String sortBy,
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
                .map(this::mapToResponse);
    }

    @Override
    public List<PointCheckinResponse> getAllCheckinPoints() {
        List<CheckinPoint> checkinPoints = checkinPointRepository.findAll();
        return checkinPoints.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PointCheckinResponse mapToResponse(CheckinPoint entity) {
        return PointCheckinResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .position(entity.getPosition())
                .isActive(entity.getIsActive())
                .qrCode(entity.getQrCode())
                .longitude(entity.getLongitude().doubleValue())
                .latitude(entity.getLatitude().doubleValue())
                .rewardPoints(entity.getRewardPoints())
                .panoramaImageUrl(entity.getPanoramaImageUrl())
                .defaultPitch(entity.getDefaultPitch())
                .defaultYaw(entity.getDefaultYaw())
                .thumbnailUrl(entity.getThumbnailUrl())
                .build();
    }

    @Override
    public PointCheckinResponse createCheckinPoint(CheckinPointRequest request) {
        Point point = pointRepository.findById(request.getPointId())
                .orElseThrow(() -> new ResourceNotFoundException("Point not found with id: " + request.getPointId()));

        CheckinPoint checkinPoint = CheckinPoint.builder()
                .point(point)
                .name(request.getName())
                .description(request.getDescription())
                .position(request.getPosition())
                .thumbnailUrl(request.getThumbnailUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .qrCode(request.getQrCode())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .rewardPoints(request.getRewardPoints())
                .panoramaImageUrl(request.getPanoramaImageUrl())
                .defaultYaw(request.getDefaultYaw() != null ? request.getDefaultYaw() : 0.0)
                .defaultPitch(request.getDefaultPitch() != null ? request.getDefaultPitch() : 0.0)
                .build();

        CheckinPoint saved = checkinPointRepository.save(checkinPoint);
        // Sync
        geoService.addCheckinToRedis(saved.getId().toString(), saved.getLongitude().doubleValue(),
                saved.getLatitude().doubleValue());
        return mapToResponse(saved);
    }

    @Override
    public PointCheckinResponse updateCheckinPoint(UUID id, CheckinPointRequest request) {
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
        if (request.getPanoramaImageUrl() != null) checkinPoint.setPanoramaImageUrl(request.getPanoramaImageUrl());
        if (request.getDefaultYaw() != null) checkinPoint.setDefaultYaw(request.getDefaultYaw());
        if (request.getDefaultPitch() != null) checkinPoint.setDefaultPitch(request.getDefaultPitch());

        CheckinPoint saved = checkinPointRepository.save(checkinPoint);
        if (request.getLatitude() != null || request.getLongitude() != null) {
            // Sync to redis if location changed
            geoService.updateCheckinInRedis(saved.getId().toString(), saved.getLongitude().doubleValue(),
                    saved.getLatitude().doubleValue());
        }
        return mapToResponse(saved);
    }

    @Override
    public void deleteCheckinPoint(UUID id, UUID currentUserId) {
        CheckinPoint checkinPoint = checkinPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + id));

        checkinPoint.setDeletedAt(java.time.LocalDateTime.now());
        checkinPoint.setDeletedBy(currentUserId);
        checkinPointRepository.save(checkinPoint);

        // Sync
        geoService.removeCheckinFromRedis(id.toString());
    }

    @Override
    public PointCheckinResponse getCheckinPointByIdForAdmin(UUID id) {
        CheckinPoint checkinPoint = checkinPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + id));
        return mapToResponse(checkinPoint);
    }

    @Override
    public Page<PointCheckinResponse> getAllCheckinPointsForAdmin(int page, int size, String sortBy, String sortDir,
            String search, boolean includeDeleted) {
        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), sort);

        return checkinPointRepository.findAll(CheckinPointSpecification.withFilters(null, search, !includeDeleted), pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<PointCheckinResponse> getNearbyCheckinPoints(double latitude, double longitude, double radiusMeters) {
        List<String> idList = geoService.getCheckinsInRadius(latitude, longitude, radiusMeters);
        // Get all the checkin point entities from the database and map them to responses
        List<UUID> uuidList = idList.stream().map(UUID::fromString).toList();
        List<CheckinPoint> checkinPoints = checkinPointRepository.findAllById(uuidList);
        return checkinPoints.stream()
                .map(this::mapToResponse)
                .toList();
    }


}
