package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.MapPointResponse;
import fpt.project.NeoNHS.dto.response.point.PointPanoramaResponse;
import fpt.project.NeoNHS.dto.response.point.PointResponse;
import fpt.project.NeoNHS.dto.response.review.ReviewImageResponse;
import fpt.project.NeoNHS.service.PanoramaService;
import fpt.project.NeoNHS.service.PointService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Tag(name = "User - Points", description = "User APIs for viewing points")
public class PointController {

    private final PointService pointService;
    private final PanoramaService panoramaService;

    @GetMapping("/{id}")
    public ApiResponse<PointResponse> getPointById(@PathVariable UUID id) {
        PointResponse data = pointService.getPointById(id);
        return ApiResponse.success(data);
    }

    @GetMapping("/all/{attractionId}")
    public ApiResponse<List<PointResponse>> getPointsByAttraction(@PathVariable UUID attractionId) {
        List<PointResponse> data = pointService.getPointsByAttraction(attractionId);
        return ApiResponse.success(data);
    }

    @GetMapping("/attraction/{attractionId}")
    public ApiResponse<Page<PointResponse>> getAllPointsWithPagination(
            @PathVariable UUID attractionId,
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "orderIndex", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.SORT_ASC, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search) {
        return ApiResponse
                .success(pointService.getAllPointsWithPagination(attractionId, page, size, sortBy, sortDir, search));
    }

    @GetMapping("/all")
    public ApiResponse<Page<PointResponse>> getAllPoints(
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = PaginationConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.SORT_ASC, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search) {
        Page<PointResponse> data = pointService.getAllPoints(page, size, sortBy, sortDir, search);
        return ApiResponse.success(data);
    }

    @GetMapping("/map")
    public ApiResponse<List<MapPointResponse>> getAllPointsOnMap() {
        List<MapPointResponse> data = pointService.getAllPointsOnMap();
        return ApiResponse.success(data);
    }

    /**
     * Get panorama data for a specific point.
     * Returns the 360° image URL, camera defaults, and all interactive hot spots.
     */
    @GetMapping("/{pointId}/panorama")
    public ApiResponse<List<PointPanoramaResponse>> getPointPanoramas(@PathVariable UUID pointId) {
        List<PointPanoramaResponse> response = panoramaService.getPanoramasByPoint(pointId);
        return ApiResponse.success("Panorama data retrieved successfully", response);
    }

    @GetMapping("/{pointId}/public-checkin-images")
    public ApiResponse<Page<ReviewImageResponse>> getPointPublicCheckinImage(
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = PaginationConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.SORT_ASC, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search,
            @PathVariable UUID pointId) {
        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        var pageable = PageRequest.of(page, size, sort);
        var response = pointService.getPointPublicCheckinImage(pointId, pageable);
        return ApiResponse.success("Point image retrieved", response);
    }
}
