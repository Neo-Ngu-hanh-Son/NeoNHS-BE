package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.request.point.PanoramaHotSpotRequest;
import fpt.project.NeoNHS.dto.request.point.PanoramaRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.LinkingPanoramaResponse;
import fpt.project.NeoNHS.dto.response.point.PanoramaHotSpotResponse;
import fpt.project.NeoNHS.dto.response.point.PointPanoramaResponse;
import fpt.project.NeoNHS.service.PanoramaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Panorama", description = "Admin APIs for managing 360° panorama views and hot spots")
public class AdminPointPanoramaController {

    private final PanoramaService panoramaService;

    // NOTE: Panorama public endpoints is at PointController
    @Operation(summary = "Add a new panorama (Along with it hotspots) to a Point",
            description = "Adds a panorama and marks it as default if it's the first one")
    @PostMapping("/points/{pointId}/panoramas")
    public ResponseEntity<ApiResponse<PointPanoramaResponse>> addPanoramaToPoint(
            @PathVariable UUID pointId,
            @Valid @RequestBody PanoramaRequest request) {
        PointPanoramaResponse response = panoramaService.addPanoramaToPoint(pointId, request);
        return ResponseEntity.ok(ApiResponse.success("Point panorama added successfully", response));
    }

    @Operation(summary = "Update an existing panorama along with it's hotspots")
    @PutMapping("/panoramas/{panoramaId}")
    public ResponseEntity<ApiResponse<PointPanoramaResponse>> updatePanorama(
            @PathVariable UUID panoramaId,
            @Valid @RequestBody PanoramaRequest request) {
        PointPanoramaResponse response = panoramaService.updatePanorama(panoramaId, request);
        return ResponseEntity.ok(ApiResponse.success("Point panorama updated successfully", response));
    }

    @Operation(summary = "Get a specific panorama by ID")
    @GetMapping("/panoramas/{panoramaId}")
    public ResponseEntity<ApiResponse<PointPanoramaResponse>> getPanoramaById(@PathVariable UUID panoramaId) {
        PointPanoramaResponse response = panoramaService.getPanoramaById(panoramaId);
        return ResponseEntity.ok(ApiResponse.success("Point panorama retrieved successfully", response));
    }

    @Operation(summary = "Get all panoramas for a Point")
    @GetMapping("/points/{pointId}/panoramas")
    public ResponseEntity<ApiResponse<List<PointPanoramaResponse>>> getPanoramasByPoint(@PathVariable UUID pointId) {
        List<PointPanoramaResponse> response = panoramaService.getPanoramasByPoint(pointId);
        return ResponseEntity.ok(ApiResponse.success("Point panoramas retrieved successfully", response));
    }

    @GetMapping("/panoramas/linking")
    public ResponseEntity<ApiResponse<List<LinkingPanoramaResponse>>> getPanoramaForLinking(@RequestParam String currentPanoramaId) {
        List<LinkingPanoramaResponse> response = panoramaService.getAllPanoramaForLinking();
        return ResponseEntity.ok(ApiResponse.success("Point panoramas retrieved successfully", response));
    }

    @Operation(summary = "Delete panorama by ID")
    @DeleteMapping("/panoramas/{panoramaId}")
    public ResponseEntity<ApiResponse<Void>> deletePanorama(@PathVariable UUID panoramaId) {
        panoramaService.deletePanorama(panoramaId);
        return ResponseEntity.ok(ApiResponse.success("Point panorama deleted successfully", null));
    }
}