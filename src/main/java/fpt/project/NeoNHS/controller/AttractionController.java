package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.attraction.AttractionRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.attraction.AttractionResponse;
import fpt.project.NeoNHS.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AttractionResponse> createAttraction(@RequestBody AttractionRequest request) {
        AttractionResponse data = attractionService.createAttraction(request);
        return ApiResponse.success(HttpStatus.CREATED, "Attraction created successfully!", data);
    }

    @GetMapping("/get-all")
    public ApiResponse<List<AttractionResponse>> getAllAttractions() {
        List<AttractionResponse> data = attractionService.getAllAttractions();
        return ApiResponse.success("Get all attractions successfully!", data);
    }

    @GetMapping("/{id}")
    public ApiResponse<AttractionResponse> getAttractionById(@PathVariable UUID id) {
        AttractionResponse data = attractionService.getAttractionById(id);
        return ApiResponse.success(data);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AttractionResponse> updateAttraction(@PathVariable UUID id, @RequestBody AttractionRequest request) {
        AttractionResponse data = attractionService.updateAttraction(id, request);
        return ApiResponse.success("Attraction updated successfully!", data);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAttraction(@PathVariable UUID id) {
        attractionService.deleteAttraction(id);
        return ApiResponse.success("Attraction deleted successfully!", null);
    }
}
