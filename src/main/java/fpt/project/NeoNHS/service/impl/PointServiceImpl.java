package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.event.EventFilterRequest;
import fpt.project.NeoNHS.dto.request.point.PointRequest;
import fpt.project.NeoNHS.dto.response.point.MapPointResponse;
import fpt.project.NeoNHS.dto.response.point.PointCheckinResponse;
import fpt.project.NeoNHS.dto.response.point.PointPanoramaResponse;
import fpt.project.NeoNHS.dto.response.point.PointResponse;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.CheckinPoint;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.helpers.AuthHelper;
import fpt.project.NeoNHS.repository.*;
import fpt.project.NeoNHS.service.PanoramaService;
import fpt.project.NeoNHS.service.PointService;
import fpt.project.NeoNHS.specification.EventSpecification;
import fpt.project.NeoNHS.specification.PointSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final AttractionRepository attractionRepository;
    private final PanoramaService panoramaService;
    private final EventRepository eventRepository;
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final UserCheckInRepository userCheckInRepository;

    @Override
    @Transactional
    public PointResponse createPoint(PointRequest request) {
        Attraction attraction = attractionRepository.findById(request.getAttractionId())
                .orElseThrow(() -> new RuntimeException("Attraction not found with id: " + request.getAttractionId()));

        Point point = Point.builder()
                .name(request.getName())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .orderIndex(request.getOrderIndex())
                .estTimeSpent(request.getEstTimeSpent())
                .type(request.getType())
                .googlePlaceId(request.getGooglePlaceId())
                .attraction(attraction)
                .build();

        return mapToResponse(pointRepository.save(point));
    }

    @Override
    @Transactional
    public PointResponse updatePoint(UUID id, PointRequest request) {
        Point point = pointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + id));

        if (request.getName() != null)
            point.setName(request.getName());
        if (request.getDescription() != null)
            point.setDescription(request.getDescription());
        if (request.getThumbnailUrl() != null)
            point.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getLatitude() != null)
            point.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            point.setLongitude(request.getLongitude());
        if (request.getOrderIndex() != null)
            point.setOrderIndex(request.getOrderIndex());
        if (request.getEstTimeSpent() != null)
            point.setEstTimeSpent(request.getEstTimeSpent());
        if (request.getType() != null)
            point.setType(request.getType());
        if (request.getGooglePlaceId() != null)
            point.setGooglePlaceId(request.getGooglePlaceId());

        if (request.getAttractionId() != null) {
            Attraction attraction = attractionRepository.findById(request.getAttractionId())
                    .orElseThrow(() -> new RuntimeException("Attraction not found"));
            point.setAttraction(attraction);
        }

        return mapToResponse(pointRepository.save(point));
    }

    @Override
    @Transactional
    public void deletePoint(UUID id, UUID userId) {
        Point point = pointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + id));

        if (point.getDeletedAt() != null) {
            throw new RuntimeException("Point is already deleted");
        }

        point.setDeletedAt(java.time.LocalDateTime.now());
        point.setDeletedBy(userId);

        pointRepository.save(point);
    }

    @Override
    public PointResponse getPointById(UUID id) {
        Point point = pointRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + id));
        return mapToResponse(point);
    }

    @Override
    public PointResponse getPointByIdForAdmin(UUID id) {
        Point point = pointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + id));
        return mapToResponse(point);
    }

    @Override
    public List<PointResponse> getPointsByAttraction(UUID attractionId) {
        if (!attractionRepository.existsById(attractionId)) {
            throw new RuntimeException("Attraction not found with id: " + attractionId);
        }

        return pointRepository.findByAttractionIdOrderByOrderIndexAsc(attractionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Page<PointResponse> getAllPointsWithPagination(UUID attractionId, int page, int size, String sortBy,
                                                          String sortDir, String search) {
        if (!attractionRepository.existsById(attractionId)) {
            throw new RuntimeException("Attraction not found");
        }

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), sort);

        return pointRepository.findByAttractionIdWithSearch(attractionId, search, pageable)
                .map(this::mapToResponse);
    }

    // Get all points and checkin points across all attractions with pagination and
    // search (if needed)
    @Override
    public Page<PointResponse> getAllPoints(int page, int size, String sortBy, String sortDir, String search) {
        return findAllPoints(page, size, sortBy, sortDir, search, true);
    }

    @Override
    public List<MapPointResponse> getAllPointsOnMap() {
        // Points already contain checkin point.
        var points = pointRepository.findAll(PointSpecification.withFilters(null, true));
        var eventFilter = EventFilterRequest.builder()
                .status(EventStatus.UPCOMING)
                .startDate(LocalDate.from(LocalDateTime.now()))
                .includeDeleted(false)
                .build();
        var events = eventRepository.findAll(EventSpecification.withFilters(eventFilter));
        var workshopTemplate = workshopTemplateRepository.findWorkshopTemplatesWithActiveUpcomingWorkshopSessions();

        var currentUser = AuthHelper.getCurrentUserPrincipalSilent();
        Set<UUID> userCheckedInIds;
        if (currentUser != null) {
            userCheckedInIds = userCheckInRepository.findCheckedInPointIdsFromUser(currentUser.getId());
        } else {
            userCheckedInIds = Collections.emptySet();
        }

        List<MapPointResponse> userMappedPoints = getMapPointWithUserCheckinDefined(points, userCheckedInIds);
        List<MapPointResponse> mapPoints = new ArrayList<>();
        mapPoints.addAll(userMappedPoints);
        mapPoints.addAll(events.stream().map(MapPointResponse::fromEventPoint).toList());
        mapPoints.addAll(workshopTemplate.stream().map(MapPointResponse::fromWorkshopTemplate).toList());
        return mapPoints;
    }

    @NotNull
    private List<MapPointResponse> getMapPointWithUserCheckinDefined(List<Point> points, Set<UUID> userCheckedInIds) {
        List<MapPointResponse> userMappedPoints = new ArrayList<>();
        for (var p : points) {
            // Note some props are not mapped because no need.
            var userCheckinPoint = MapPointResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .thumbnailUrl(p.getThumbnailUrl())
                    .latitude(p.getLatitude().doubleValue())
                    .longitude(p.getLongitude().doubleValue())
                    .type(p.getType())
                    .attractionId(p.getAttraction() != null ? p.getAttraction().getId() : null)
                    .googlePlaceId(p.getGooglePlaceId())
                    .checkinPoints(p.getCheckinPoints().stream()
                            .map(cp -> {
                                boolean isUserCheckedInHere = userCheckedInIds.contains(cp.getId());
                                return PointCheckinResponse.fromEntity(cp, isUserCheckedInHere);
                            })
                            .toList())
                    .build();
            userMappedPoints.add(userCheckinPoint);
        }
        return userMappedPoints;
    }

    @Override
    public Page<PointResponse> getAllPointsForAdmin(int page, int size, String sortBy, String sortDir, String search,
                                                    boolean includeDeleted) {
        return findAllPoints(page, size, sortBy, sortDir, search, !includeDeleted);
    }

    private Page<PointResponse> findAllPoints(int page, int size, String sortBy, String sortDir, String search,
                                              boolean excludeDeleted) {
        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), sort);

        return pointRepository.findAll(PointSpecification.withFilters(search, excludeDeleted), pageable)
                .map(this::mapToResponse);
    }

    private PointResponse mapToResponse(Point entity) {
        int historyAudioCount = (int) entity.getHistoryAudios().stream()
                .filter(historyAudio -> historyAudio.getDeletedAt() == null)
                .count();
        return PointResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .thumbnailUrl(entity.getThumbnailUrl())
                .latitude(entity.getLatitude().doubleValue())
                .longitude(entity.getLongitude().doubleValue())
                .orderIndex(entity.getOrderIndex())
                .estTimeSpent(entity.getEstTimeSpent())
                .type(entity.getType())
                .attractionId(entity.getAttraction().getId())
                .panoramaImageUrl(entity.getPanoramaImageUrl())
                .defaultPitch(entity.getDefaultPitch())
                .defaultYaw(entity.getDefaultYaw())
                .googlePlaceId(entity.getGooglePlaceId())
                .historyAudioCount(historyAudioCount)
                .build();
    }

    @Override
    public PointPanoramaResponse getPointPanorama(UUID pointId) {
        return panoramaService.getPointPanorama(pointId);
    }
}
