package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.AttractionResponse;
import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.AttractionRepository;
import fpt.project.NeoNHS.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AttractionService for handling attraction operations
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttractionServiceImpl implements AttractionService {

    private final AttractionRepository attractionRepository;

    /**
     * Get all active attractions with pagination and optional search.
     *
     * @param keyword Optional search keyword for attraction name
     * @param page    Page number
     * @param size    Page size
     * @param sortBy  Field to sort by
     * @param sortDir Sort direction - asc or desc
     * @return Paged response of attractions
     */
    @Override
    public PagedResponse<AttractionResponse> getAllActiveAttractions(
            String keyword, int page, int size, String sortBy, String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Attraction> attractionPage = attractionRepository.findActiveAttractions(keyword, pageable);

        List<AttractionResponse> content = attractionPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<AttractionResponse>builder()
                .content(content)
                .page(attractionPage.getNumber())
                .size(attractionPage.getSize())
                .totalElements(attractionPage.getTotalElements())
                .totalPages(attractionPage.getTotalPages())
                .first(attractionPage.isFirst())
                .last(attractionPage.isLast())
                .empty(attractionPage.isEmpty())
                .build();
    }

    @Override
    public AttractionResponse getActiveAttractionById(UUID id) {
        Attraction attraction = attractionRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", id.toString()));
        return mapToResponse(attraction);
    }

    @Override
    public long countActiveAttractions() {
        return attractionRepository.countByIsActiveTrue();
    }

    /**
     * Maps an Attraction entity to AttractionResponse DTO
     *
     * @param attraction The attraction entity
     * @return The mapped DTO
     */
    private AttractionResponse mapToResponse(Attraction attraction) {
        return AttractionResponse.builder()
                .id(attraction.getId())
                .name(attraction.getName())
                .description(attraction.getDescription())
                .mapImageUrl(attraction.getMapImageUrl())
                .address(attraction.getAddress())
                .latitude(attraction.getLatitude())
                .longitude(attraction.getLongitude())
                .status(attraction.getStatus())
                .thumbnailUrl(attraction.getThumbnailUrl())
                .openHour(attraction.getOpenHour())
                .closeHour(attraction.getCloseHour())
                .pointCount(attraction.getPoints() != null ? attraction.getPoints().size() : 0)
                .createdAt(attraction.getCreatedAt())
                .updatedAt(attraction.getUpdatedAt())
                .build();
    }
}

