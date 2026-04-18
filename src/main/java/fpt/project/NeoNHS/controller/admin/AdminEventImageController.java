package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventImageResponse;
import fpt.project.NeoNHS.service.EventImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Admin controller for managing Event Image gallery.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/events/{eventId}/images")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Event Images", description = "Admin APIs for managing event image gallery (requires ADMIN role)")
public class AdminEventImageController {

    private final EventImageService eventImageService;

    @Operation(
            summary = "Get event gallery",
            description = "Retrieve all images for a specific event"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventImageResponse>>> getEventImages(
            @Parameter(description = "Event ID") @PathVariable UUID eventId) {
        List<EventImageResponse> images = eventImageService.getImagesByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Event images retrieved successfully", images));
    }

    @Operation(
            summary = "Upload images to event gallery",
            description = "Upload one or more images to an event's gallery. Images are uploaded to Cloudinary automatically."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<EventImageResponse>>> uploadImages(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @RequestParam("images") MultipartFile[] images) {
        List<EventImageResponse> uploaded = eventImageService.uploadImages(eventId, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Images uploaded successfully", uploaded));
    }

    @Operation(
            summary = "Bulk delete event images",
            description = "Soft-delete multiple images from the event gallery. Cannot delete any image that is currently the thumbnail."
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteImages(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @RequestBody List<UUID> imageIds) {
        eventImageService.deleteImages(eventId, imageIds);
        return ResponseEntity.ok(ApiResponse.success("Images deleted successfully", null));
    }

    @Operation(
            summary = "Delete an event image",
            description = "Soft-delete a specific image from the event gallery. Cannot delete the current thumbnail."
    )
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @Parameter(description = "Image ID") @PathVariable UUID imageId) {
        eventImageService.deleteImage(eventId, imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully", null));
    }

    @Operation(
            summary = "Set image as thumbnail",
            description = "Set a specific image as the event's thumbnail. The previous thumbnail will be demoted to a regular gallery image."
    )
    @PatchMapping("/{imageId}/set-thumbnail")
    public ResponseEntity<ApiResponse<EventImageResponse>> setThumbnail(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @Parameter(description = "Image ID") @PathVariable UUID imageId) {
        EventImageResponse image = eventImageService.setThumbnail(eventId, imageId);
        return ResponseEntity.ok(ApiResponse.success("Thumbnail updated successfully", image));
    }
}
