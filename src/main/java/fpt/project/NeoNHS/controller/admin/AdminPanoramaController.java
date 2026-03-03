package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.request.point.PanoramaHotSpotRequest;
import fpt.project.NeoNHS.dto.request.point.PanoramaRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
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
@RequestMapping("/api/admin/panorama")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Panorama", description = "Admin APIs for managing 360° panorama views and hot spots")
public class AdminPanoramaController {

  private final PanoramaService panoramaService;

  // NOTE: Panorama public endpoints is at PointController

  // ═══════════════════════════════════════════════════════
  // Point Panorama endpoints
  // ═══════════════════════════════════════════════════════

  @Operation(summary = "Create or update panorama for a Point", description = "Sets the panorama image, camera defaults, and replaces all hot spots for the specified Point")
  @PutMapping("/points/{pointId}")
  public ResponseEntity<ApiResponse<PointPanoramaResponse>> createOrUpdatePointPanorama(
      @PathVariable UUID pointId,
      @Valid @RequestBody PanoramaRequest request) {
    PointPanoramaResponse response = panoramaService.createOrUpdatePointPanorama(pointId, request);
    return ResponseEntity.ok(ApiResponse.success("Point panorama saved successfully", response));
  }

  @Operation(summary = "Get panorama for a Point")
  @GetMapping("/points/{pointId}")
  public ResponseEntity<ApiResponse<PointPanoramaResponse>> getPointPanorama(@PathVariable UUID pointId) {
    PointPanoramaResponse response = panoramaService.getPointPanorama(pointId);
    return ResponseEntity.ok(ApiResponse.success("Point panorama retrieved successfully", response));
  }

  @Operation(summary = "Delete panorama from a Point", description = "Removes the panorama image URL, resets camera defaults, and deletes all hot spots")
  @DeleteMapping("/points/{pointId}")
  public ResponseEntity<ApiResponse<Void>> deletePointPanorama(@PathVariable UUID pointId) {
    panoramaService.deletePointPanorama(pointId);
    return ResponseEntity.ok(ApiResponse.success("Point panorama deleted successfully", null));
  }

  // ═══════════════════════════════════════════════════════
  // CheckinPoint Panorama endpoints
  // ═══════════════════════════════════════════════════════

  @Operation(summary = "Create or update panorama for a CheckinPoint", description = "Sets the panorama image, camera defaults, and replaces all hot spots for the specified CheckinPoint")
  @PutMapping("/checkin-points/{checkinPointId}")
  public ResponseEntity<ApiResponse<PointPanoramaResponse>> createOrUpdateCheckinPointPanorama(
      @PathVariable UUID checkinPointId,
      @Valid @RequestBody PanoramaRequest request) {
    PointPanoramaResponse response = panoramaService.createOrUpdateCheckinPointPanorama(checkinPointId, request);
    return ResponseEntity.ok(ApiResponse.success("CheckinPoint panorama saved successfully", response));
  }

  @Operation(summary = "Get panorama for a CheckinPoint")
  @GetMapping("/checkin-points/{checkinPointId}")
  public ResponseEntity<ApiResponse<PointPanoramaResponse>> getCheckinPointPanorama(
      @PathVariable UUID checkinPointId) {
    PointPanoramaResponse response = panoramaService.getCheckinPointPanorama(checkinPointId);
    return ResponseEntity.ok(ApiResponse.success("CheckinPoint panorama retrieved successfully", response));
  }

  @Operation(summary = "Delete panorama from a CheckinPoint")
  @DeleteMapping("/checkin-points/{checkinPointId}")
  public ResponseEntity<ApiResponse<Void>> deleteCheckinPointPanorama(@PathVariable UUID checkinPointId) {
    panoramaService.deleteCheckinPointPanorama(checkinPointId);
    return ResponseEntity.ok(ApiResponse.success("CheckinPoint panorama deleted successfully", null));
  }

  // ═══════════════════════════════════════════════════════
  // Individual HotSpot CRUD endpoints
  // ═══════════════════════════════════════════════════════

  @Operation(summary = "Add a single hot spot to a Point's panorama")
  @PostMapping("/points/{pointId}/hotspots")
  public ResponseEntity<ApiResponse<PanoramaHotSpotResponse>> addHotSpotToPoint(
      @PathVariable UUID pointId,
      @Valid @RequestBody PanoramaHotSpotRequest request) {
    PanoramaHotSpotResponse response = panoramaService.addHotSpotToPoint(pointId, request);
    return ResponseEntity.ok(ApiResponse.success("Hot spot added successfully", response));
  }

  @Operation(summary = "Add a single hot spot to a CheckinPoint's panorama")
  @PostMapping("/checkin-points/{checkinPointId}/hotspots")
  public ResponseEntity<ApiResponse<PanoramaHotSpotResponse>> addHotSpotToCheckinPoint(
      @PathVariable UUID checkinPointId,
      @Valid @RequestBody PanoramaHotSpotRequest request) {
    PanoramaHotSpotResponse response = panoramaService.addHotSpotToCheckinPoint(checkinPointId, request);
    return ResponseEntity.ok(ApiResponse.success("Hot spot added successfully", response));
  }

  @Operation(summary = "Get all hot spots for a Point")
  @GetMapping("/points/{pointId}/hotspots")
  public ResponseEntity<ApiResponse<List<PanoramaHotSpotResponse>>> getHotSpotsByPoint(
      @PathVariable UUID pointId) {
    List<PanoramaHotSpotResponse> response = panoramaService.getHotSpotsByPoint(pointId);
    return ResponseEntity.ok(ApiResponse.success("Hot spots retrieved successfully", response));
  }

  @Operation(summary = "Get all hot spots for a CheckinPoint")
  @GetMapping("/checkin-points/{checkinPointId}/hotspots")
  public ResponseEntity<ApiResponse<List<PanoramaHotSpotResponse>>> getHotSpotsByCheckinPoint(
      @PathVariable UUID checkinPointId) {
    List<PanoramaHotSpotResponse> response = panoramaService.getHotSpotsByCheckinPoint(checkinPointId);
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
