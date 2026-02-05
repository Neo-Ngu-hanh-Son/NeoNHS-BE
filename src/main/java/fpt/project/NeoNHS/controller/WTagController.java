package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.workshop.CreateWTagRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWTagRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.workshop.WTagResponse;
import fpt.project.NeoNHS.service.WTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wtags")
@RequiredArgsConstructor
public class WTagController {

    private final WTagService wTagService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WTagResponse>> createWTag(
            @Valid @RequestBody CreateWTagRequest request) {
        WTagResponse response = wTagService.createWTag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Workshop tag created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WTagResponse>> getWTagById(@PathVariable UUID id) {
        WTagResponse response = wTagService.getWTagById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Workshop tag retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WTagResponse>>> getAllWTags() {
        List<WTagResponse> response = wTagService.getAllWTags();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Workshop tags retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WTagResponse>> updateWTag(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWTagRequest request) {
        WTagResponse response = wTagService.updateWTag(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Workshop tag updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWTag(@PathVariable UUID id) {
        wTagService.deleteWTag(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Workshop tag deleted successfully", null));
    }
}
