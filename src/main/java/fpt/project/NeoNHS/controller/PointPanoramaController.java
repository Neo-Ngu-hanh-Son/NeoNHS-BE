package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.PointPanoramaResponse;
import fpt.project.NeoNHS.service.PanoramaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/panoramas")
@RequiredArgsConstructor
public class PointPanoramaController {
    private final PanoramaService panoramaService;

    /**
     * Get specific panorama by id
     */
    @GetMapping("/{panoramaId}")
    public ApiResponse<PointPanoramaResponse> getPanoramaByid(@PathVariable UUID panoramaId) {
        PointPanoramaResponse response = panoramaService.getPanoramaById(panoramaId);
        return ApiResponse.success("Panorama data retrieved successfully", response);
    }
}
