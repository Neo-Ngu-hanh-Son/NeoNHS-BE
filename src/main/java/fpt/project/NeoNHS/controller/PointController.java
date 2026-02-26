package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.point.PointRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.PointResponse;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PointResponse> createPoint(@RequestBody PointRequest request) {
        PointResponse data = pointService.createPoint(request);
        return ApiResponse.success(HttpStatus.CREATED, "Point created successfully", data);
    }

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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PointResponse> updatePoint(@PathVariable UUID id, @RequestBody PointRequest request) {
        PointResponse data = pointService.updatePoint(id, request);
        return ApiResponse.success("Point updated successfully", data);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deletePoint(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        pointService.deletePoint(id, currentUser.getId());
        return ApiResponse.success("Point deleted successfully", null);
    }

    @GetMapping("/all")
    public ApiResponse<Page<PointResponse>> getAllPoints(
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "orderIndex", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.SORT_ASC, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String searc
    ) {
        Page<PointResponse> data = pointService.getAllPoints(page, size, sortBy, sortDir, searc);
        return ApiResponse.success(data);
    }
}