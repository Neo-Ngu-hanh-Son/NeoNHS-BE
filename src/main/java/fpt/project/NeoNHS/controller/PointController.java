package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.point.PointRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.PointResponse;
import fpt.project.NeoNHS.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PointResponse> createPoint(@RequestBody PointRequest request) {
        PointResponse data = pointService.createPoint(request);
        return ApiResponse.success(HttpStatus.CREATED, "Point created successfully", data);
    }

    @GetMapping("/{id}")
    public ApiResponse<PointResponse> getPointById(@PathVariable UUID id) {
        PointResponse data = pointService.getPointById(id);
        return ApiResponse.success(data);
    }

    @GetMapping("/attraction/{attractionId}")
    public ApiResponse<List<PointResponse>> getPointsByAttraction(@PathVariable UUID attractionId) {
        List<PointResponse> data = pointService.getPointsByAttraction(attractionId);
        return ApiResponse.success(data);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PointResponse> updatePoint(@PathVariable UUID id, @RequestBody PointRequest request) {
        PointResponse data = pointService.updatePoint(id, request);
        return ApiResponse.success("Point updated successfully", data);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deletePoint(@PathVariable UUID id) {
        pointService.deletePoint(id);
        return ApiResponse.success("Point deleted successfully", null);
    }
}