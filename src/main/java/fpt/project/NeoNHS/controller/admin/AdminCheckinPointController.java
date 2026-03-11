package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.point.CheckinPointRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.PointCheckinResponse;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.CheckinPointService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/checkin-points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Checkin Points", description = "Admin APIs for managing checkin points (requires ADMIN role)")
public class AdminCheckinPointController {

    private final CheckinPointService checkinPointService;

    @PostMapping
    public ApiResponse<PointCheckinResponse> createCheckinPoint(@RequestBody CheckinPointRequest request) {
        PointCheckinResponse data = checkinPointService.createCheckinPoint(request);
        return ApiResponse.success(HttpStatus.CREATED, "CheckinPoint created successfully", data);
    }

    @GetMapping("/{id}")
    public ApiResponse<PointCheckinResponse> getCheckinPointById(@PathVariable UUID id) {
        PointCheckinResponse data = checkinPointService.getCheckinPointByIdForAdmin(id);
        return ApiResponse.success(data);
    }

    @GetMapping("/all")
    public ApiResponse<Page<PointCheckinResponse>> getAllCheckinPoints(
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "name", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.SORT_ASC, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "includeDeleted", defaultValue = "true", required = false) boolean includeDeleted) {
        Page<PointCheckinResponse> data = checkinPointService.getAllCheckinPointsForAdmin(page, size, sortBy, sortDir, search, includeDeleted);
        return ApiResponse.success(data);
    }

    @PutMapping("/{id}")
    public ApiResponse<PointCheckinResponse> updateCheckinPoint(@PathVariable UUID id, @RequestBody CheckinPointRequest request) {
        PointCheckinResponse data = checkinPointService.updateCheckinPoint(id, request);
        return ApiResponse.success("CheckinPoint updated successfully", data);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCheckinPoint(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        checkinPointService.deleteCheckinPoint(id, currentUser.getId());
        return ApiResponse.success("CheckinPoint deleted successfully", null);
    }
}
