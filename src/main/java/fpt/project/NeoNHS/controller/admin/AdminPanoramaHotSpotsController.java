package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.request.point.PanoramaHotSpotRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.PanoramaHotSpotResponse;
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
@Tag(name = "Admin - Panoramas/Hotspots", description = "Admin manage hot spots of panoramas")
public class AdminPanoramaHotSpotsController {
    private final PanoramaService panoramaService;

    @Operation(summary = "Add a single hot spot to a panorama")
    @PostMapping("/panoramas/{panoramaId}/hotspots")
    public ResponseEntity<ApiResponse<PanoramaHotSpotResponse>> addHotSpotToPanorama(
            @PathVariable UUID panoramaId,
            @Valid @RequestBody PanoramaHotSpotRequest request) {
        PanoramaHotSpotResponse response = panoramaService.addHotSpotToPanorama(panoramaId, request);
        return ResponseEntity.ok(ApiResponse.success("Hot spot added successfully", response));
    }

    @Operation(summary = "Add many single hot spot to a panorama")
    @PostMapping("/panoramas/{panoramaId}/hotspots/batch")
    public ResponseEntity<ApiResponse<PanoramaHotSpotResponse>> addHotSpotToPanoramaBatch(
            @PathVariable UUID panoramaId,
            @Valid @RequestBody PanoramaHotSpotRequest request) {
        PanoramaHotSpotResponse response = panoramaService.addHotSpotToPanorama(panoramaId, request);
        return ResponseEntity.ok(ApiResponse.success("Hot spot added successfully", response));
    }

    @Operation(summary = "Get all hot spots for a panorama")
    @GetMapping("/panoramas/{panoramaId}/hotspots")
    public ResponseEntity<ApiResponse<List<PanoramaHotSpotResponse>>> getHotSpotsByPanorama(
            @PathVariable UUID panoramaId) {
        List<PanoramaHotSpotResponse> response = panoramaService.getHotSpotsByPanorama(panoramaId);
        return ResponseEntity.ok(ApiResponse.success("Hot spots retrieved successfully", response));
    }

    @Operation(summary = "Update an existing hot spot")
    @PutMapping("/hotspots/{hotSpotId}")
    public ResponseEntity<ApiResponse<PanoramaHotSpotResponse>> updateHotSpot(
            @PathVariable UUID hotSpotId,
            @Valid @RequestBody PanoramaHotSpotRequest request) {
        PanoramaHotSpotResponse response = panoramaService.updateHotSpot(hotSpotId, request);
        return ResponseEntity.ok(ApiResponse.success("Hot spot updated successfully", response));
    }

    @Operation(summary = "Delete a hot spot")
    @DeleteMapping("/hotspots/{hotSpotId}")
    public ResponseEntity<ApiResponse<Void>> deleteHotSpot(@PathVariable UUID hotSpotId) {
        panoramaService.deleteHotSpot(hotSpotId);
        return ResponseEntity.ok(ApiResponse.success("Hot spot deleted successfully", null));
    }
}