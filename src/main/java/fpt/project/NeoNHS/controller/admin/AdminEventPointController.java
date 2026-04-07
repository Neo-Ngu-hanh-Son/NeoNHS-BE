package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.request.event.EventPointRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventPointResponse;
import fpt.project.NeoNHS.service.EventPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/event-points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Event Points", description = "Admin APIs for managing event points")
public class AdminEventPointController {

    private final EventPointService pointService;

    @PostMapping
    @Operation(summary = "Create a new event point")
    public ResponseEntity<ApiResponse<EventPointResponse>> createPoint(
            @Valid @RequestBody EventPointRequest request) {
        EventPointResponse response = pointService.createPoint(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Point created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing event point")
    public ResponseEntity<ApiResponse<EventPointResponse>> updatePoint(
            @PathVariable UUID id, @Valid @RequestBody EventPointRequest request) {
        EventPointResponse response = pointService.updatePoint(id, request);
        return ResponseEntity.ok(ApiResponse.success("Point updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get point by ID")
    public ResponseEntity<ApiResponse<EventPointResponse>> getPointById(@PathVariable UUID id) {
        EventPointResponse response = pointService.getPointById(id);
        return ResponseEntity.ok(ApiResponse.success("Point retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all points")
    public ResponseEntity<ApiResponse<List<EventPointResponse>>> getAllPoints() {
        List<EventPointResponse> response = pointService.getAllPoints();
        return ResponseEntity.ok(ApiResponse.success("Points retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete point")
    public ResponseEntity<ApiResponse<Void>> deletePoint(@PathVariable UUID id) {
        pointService.deletePoint(id);
        return ResponseEntity.ok(ApiResponse.success("Point deleted successfully", null));
    }
}
