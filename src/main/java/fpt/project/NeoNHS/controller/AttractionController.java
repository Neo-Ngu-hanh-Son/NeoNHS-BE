package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.AttractionResponse;
import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.service.AttractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for public Attraction API endpoints.
 * All endpoints in this controller are publicly accessible.
 */
@RestController
@RequestMapping("/api/public/attractions")
@RequiredArgsConstructor
@Tag(name = "Attraction", description = "Public Attraction API - Browse tourism attractions")
public class AttractionController {

    private final AttractionService attractionService;

    @GetMapping
    @Operation(
            summary = "Get all attractions",
            description = "Retrieve all active attractions with pagination, sorting, and optional keyword search"
    )
    public ResponseEntity<ApiResponse<PagedResponse<AttractionResponse>>> getAllAttractions(
            @Parameter(description = "Search keyword for attraction name")
            @RequestParam(value = "keyword", required = false) String keyword,
            
            @Parameter(description = "Page number (from 0)")
            @RequestParam(value = "page", defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page")
            @RequestParam(value = "size", defaultValue = "10") int size,
            
            @Parameter(description = "Field to sort by")
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir
    ) {
        PagedResponse<AttractionResponse> attractions = attractionService
                .getAllActiveAttractions(keyword, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "Attractions retrieved successfully", attractions)
        );
    }

    /**
     * Get a single attraction by its ID.
     *
     * @param id Attraction UUID
     * @return Attraction details
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get attraction by ID",
            description = "Retrieve a single active attraction by its UUID"
    )
    public ResponseEntity<ApiResponse<AttractionResponse>> getAttractionById(
            @Parameter(description = "Attraction UUID")
            @PathVariable UUID id
    ) {
        AttractionResponse attraction = attractionService.getActiveAttractionById(id);
        
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "Attraction retrieved successfully", attraction)
        );
    }

    /**
     * Get the count of all active attractions.
     *
     * @return Count of active attractions
     */
    @GetMapping("/count")
    @Operation(
            summary = "Get attraction count",
            description = "Get the total count of active attractions"
    )
    public ResponseEntity<ApiResponse<Long>> getAttractionCount() {
        long count = attractionService.countActiveAttractions();
        
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "Attraction count retrieved successfully", count)
        );
    }
}

