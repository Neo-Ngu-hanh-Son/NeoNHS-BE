package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.point.CreatePointHistoryAudio;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.PointHistoryAudioResponse;
import fpt.project.NeoNHS.service.PointHistoryAudioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/points/{pointId}/history-audios")
@RequiredArgsConstructor
public class HistoryAudioController {

    private final PointHistoryAudioService pointHistoryAudioService;
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PointHistoryAudioResponse>> getHistoryAudio(
            @PathVariable UUID pointId,
            @PathVariable UUID id) {
        PointHistoryAudioResponse response = pointHistoryAudioService.getByPointIdAndId(pointId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PointHistoryAudioResponse>>> getHistoryAudiosByPoint(
            @PathVariable UUID pointId) {
        List<PointHistoryAudioResponse> responses = pointHistoryAudioService.getAllByPointId(pointId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

}
