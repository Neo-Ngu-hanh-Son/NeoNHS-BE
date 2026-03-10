package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.attraction.AttractionResponse;
import fpt.project.NeoNHS.service.AttractionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
@Tag(name = "User - Attractions", description = "User APIs for viewing attractions")
public class AttractionController {

    private final AttractionService attractionService;

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
}
