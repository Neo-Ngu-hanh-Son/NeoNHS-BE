package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.attraction.AttractionRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.attraction.AttractionResponse;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.AttractionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/attractions")
@RequiredArgsConstructor
@Tag(name = "Admin - Attractions", description = "Admin APIs for managing attractions (requires ADMIN role)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAttractionController {

    private final AttractionService attractionService;

    @PostMapping
    public ApiResponse<AttractionResponse> createAttraction(@RequestBody AttractionRequest request) {
        AttractionResponse data = attractionService.createAttraction(request);
        return ApiResponse.success(HttpStatus.CREATED, "Attraction created successfully!", data);
    }

    @GetMapping
    public ApiResponse<Page<AttractionResponse>> getAllAttractionsForAdmin(
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE, required = false) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = PaginationConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.DEFAULT_SORT_DIR, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "includeInactive", defaultValue = "true", required = false) boolean includeInactive
    ) {
        return ApiResponse.success(
                attractionService.getAllAttractionsWithPaginationForAdmin(
                        page, size, sortBy, sortDir, search, includeInactive
                )
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AttractionResponse> getAttractionByIdForAdmin(@PathVariable UUID id) {
        AttractionResponse data = attractionService.getAttractionByIdForAdmin(id);
        return ApiResponse.success(data);
    }

    @PutMapping("/{id}")
    public ApiResponse<AttractionResponse> updateAttraction(@PathVariable UUID id, @RequestBody AttractionRequest request) {
        AttractionResponse data = attractionService.updateAttraction(id, request);
        return ApiResponse.success("Attraction updated successfully!", data);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAttraction(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        attractionService.deleteAttraction(id, currentUser.getId());
        return ApiResponse.success("Attraction deleted successfully!", null);
    }
}
