package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.point.PointRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.PointResponse;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.PointService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Points", description = "Admin APIs for managing points (requires ADMIN role)")
public class AdminPointController {

    private final PointService pointService;

    @PostMapping
    public ApiResponse<PointResponse> createPoint(@RequestBody PointRequest request) {
        PointResponse data = pointService.createPoint(request);
        return ApiResponse.success(HttpStatus.CREATED, "Point created successfully", data);
    }

    @GetMapping("/{id}")
    public ApiResponse<PointResponse> getPointById(@PathVariable UUID id) {
        PointResponse data = pointService.getPointByIdForAdmin(id);
        return ApiResponse.success(data);
    }

    @GetMapping("/attraction/{attractionId}")
    public ApiResponse<Page<PointResponse>> getAllPointsWithPagination(
            @PathVariable UUID attractionId,
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.SORT_DESC, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search) {
        return ApiResponse.success(
                pointService.getAllPointsWithPagination(attractionId, page, size, sortBy, sortDir, search));
    }

    @GetMapping("/all")
    public ApiResponse<Page<PointResponse>> getAllPoints(
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.SORT_DESC, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "includeDeleted", defaultValue = "true", required = false) boolean includeDeleted) {
        Page<PointResponse> data = pointService.getAllPointsForAdmin(page, size, sortBy, sortDir, search,
                includeDeleted);
        return ApiResponse.success(data);
    }

    @PutMapping("/{id}")
    public ApiResponse<PointResponse> updatePoint(@PathVariable UUID id, @RequestBody PointRequest request) {
        PointResponse data = pointService.updatePoint(id, request);
        return ApiResponse.success("Point updated successfully", data);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePoint(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        pointService.deletePoint(id, currentUser.getId());
        return ApiResponse.success("Point deleted successfully", null);
    }

    @DeleteMapping("/{id}/hard")
    public ApiResponse<Void> hardDeletePoint(@PathVariable UUID id) {
        pointService.hardDeletePoint(id);
        return ApiResponse.success("Point hard deleted successfully", null);
    }
}
