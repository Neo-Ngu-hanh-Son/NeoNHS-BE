package fpt.project.NeoNHS.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.CheckinPointResponse;
import fpt.project.NeoNHS.service.CheckinPointService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/points/{pointId}/check-ins")
@RequiredArgsConstructor
@Tag(name = "User - Points - Checkin Points", description = "User APIs for viewing checkin points of a user location")
public class CheckinPointController {

    private final CheckinPointService checkinPointService;

    @GetMapping("/{checkinId}")
    public ResponseEntity<ApiResponse<CheckinPointResponse>> getPointById(@PathVariable UUID pointId,
                                                                          @PathVariable UUID checkinId) {
        CheckinPointResponse data = checkinPointService.getCheckinPointById(pointId, checkinId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CheckinPointResponse>>> getAllCheckinsOfPoint(
            @PathVariable UUID pointId,
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = PaginationConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.SORT_ASC, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search) {
        Page<CheckinPointResponse> points = checkinPointService.getAllCheckinFromPointId(pointId, page, size, sortBy,
                sortDir,
                search);
        return ResponseEntity.ok(ApiResponse.success(points));
    }

    // This get all checkin point without a care to their parent. Which could be more performant
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CheckinPointResponse>>> getAllCheckinPoints() {
        List<CheckinPointResponse> points = checkinPointService.getAllCheckinPoints();
        return ResponseEntity.ok(ApiResponse.success(points));
    }


    /**
     * Get nearby checkin points based on the user's current location.
     * This is used to determine which checkin points are close to the user and can be checked in. The radius is in meters.
     * @param latitude
     * @param longitude
     * @param metersRadius
     * @return
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<CheckinPointResponse>>> getNearbyCheckinPoints(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double metersRadius) {
        List<CheckinPointResponse> nearbyCheckins = checkinPointService.getNearbyCheckinPoints(latitude, longitude, metersRadius);
        return ResponseEntity.ok(ApiResponse.success(nearbyCheckins));
    }
}
