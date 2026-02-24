package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.point.PointRequest;
import fpt.project.NeoNHS.dto.response.point.PointResponse;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.repository.AttractionRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.service.PointService;
import fpt.project.NeoNHS.specification.AttractionSpecification;
import fpt.project.NeoNHS.specification.PointSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final AttractionRepository attractionRepository;

    @Override
    @Transactional
    public PointResponse createPoint(PointRequest request) {
        Attraction attraction = attractionRepository.findById(request.getAttractionId())
                .orElseThrow(() -> new RuntimeException("Attraction not found with id: " + request.getAttractionId()));

        Point point = Point.builder()
                .name(request.getName())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .history(request.getHistory())
                .historyAudioUrl(request.getHistoryAudioUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .orderIndex(request.getOrderIndex())
                .estTimeSpent(request.getEstTimeSpent())
                .type(request.getType())
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
        if (request.getHistory() != null)
            point.setHistory(request.getHistory());
        if (request.getHistoryAudioUrl() != null)
            point.setHistoryAudioUrl(request.getHistoryAudioUrl());
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

        if (request.getAttractionId() != null) {
            Attraction attraction = attractionRepository.findById(request.getAttractionId())
                    .orElseThrow(() -> new RuntimeException("Attraction not found"));
            point.setAttraction(attraction);
        }

        return mapToResponse(pointRepository.save(point));
    }

    @Override
    @Transactional
    public PointResponse deletePoint(UUID id) {
        if (!pointRepository.existsById(id)) {
            throw new RuntimeException("Point not found with id: " + id);
        }
        pointRepository.deleteById(id);
        return null;
    }

    @Override
    public PointResponse getPointById(UUID id) {
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

//    Get all points across all attractions with pagination and search (if needed)
    @Override
    public Page<PointResponse> getAllPoints(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), sort);

        return pointRepository.findAll(PointSpecification.withFilters(search), pageable)
                .map(this::mapToResponse);
    }

    private PointResponse mapToResponse(Point entity) {
        return PointResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .thumbnailUrl(entity.getThumbnailUrl())
                .history(entity.getHistory())
                .historyAudioUrl(entity.getHistoryAudioUrl())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .orderIndex(entity.getOrderIndex())
                .estTimeSpent(entity.getEstTimeSpent())
                .type(entity.getType())
                .attractionId(entity.getAttraction().getId())
                .build();
    }
}
