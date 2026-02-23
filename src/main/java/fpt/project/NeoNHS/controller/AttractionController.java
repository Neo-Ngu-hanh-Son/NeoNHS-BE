package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.attraction.AttractionRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.attraction.AttractionResponse;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AttractionResponse> createAttraction(@RequestBody AttractionRequest request) {
        AttractionResponse data = attractionService.createAttraction(request);
        return ApiResponse.success(HttpStatus.CREATED, "Attraction created successfully!", data);
    }

    @GetMapping("/all")
    public ApiResponse<List<AttractionResponse>> getAllAttractions() {
        List<AttractionResponse> data = attractionService.getAllAttractions();
        return ApiResponse.success("Get all attractions successfully!", data);
    }

    @GetMapping
    public ApiResponse<Page<AttractionResponse>> getAllAttractionsWithPagination(
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = PaginationConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.DEFAULT_SORT_DIR, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search
    ) {
        return ApiResponse.success(attractionService.getAllAttractionsWithPagination(page, size, sortBy, sortDir, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<AttractionResponse> getAttractionById(@PathVariable UUID id) {
        AttractionResponse data = attractionService.getAttractionById(id);
        return ApiResponse.success(data);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AttractionResponse> updateAttraction(@PathVariable UUID id, @RequestBody AttractionRequest request) {
        AttractionResponse data = attractionService.updateAttraction(id, request);
        return ApiResponse.success("Attraction updated successfully!", data);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAttraction(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        attractionService.deleteAttraction(id, currentUser.getId());
        return ApiResponse.success("Attraction deleted successfully!", null);
    }
}
