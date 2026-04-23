package fpt.project.NeoNHS.controller.admin;

import com.fasterxml.jackson.databind.JsonNode;
import fpt.project.NeoNHS.dto.request.point.CreatePointHistoryAudio;
import fpt.project.NeoNHS.dto.request.point.historyAudio.CreateSpeechFromTextRequest;
import fpt.project.NeoNHS.dto.request.point.historyAudio.HistoryAudioTranslateRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.point.historyAudio.ForcedAlignmentResponse;
import fpt.project.NeoNHS.dto.response.point.historyAudio.HistoryAudioTranslationObject;
import fpt.project.NeoNHS.dto.response.point.historyAudio.PointHistoryAudioResponse;
import fpt.project.NeoNHS.service.PointHistoryAudioService;
import jakarta.validation.Valid; // Ensure you have this import
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/points/{pointId}/history-audios")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminHistoryAudioController {

    private final PointHistoryAudioService pointHistoryAudioService;

    @PostMapping
    public ResponseEntity<ApiResponse<PointHistoryAudioResponse>> createHistoryAudio(
            @PathVariable UUID pointId,
            @Valid @RequestBody CreatePointHistoryAudio request) {
        request.setPointId(pointId);
        PointHistoryAudioResponse response = pointHistoryAudioService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "History audio created successfully", response));
    }

//    @PostMapping('/multiple')
//    public ResponseEntity<ApiResponse<Void>> createMultipleAudio(
//            @PathVariable UUID pointId,
//            @Valid @RequestBody CreateMultiplePointHistoryAudioRequest request) {
//        pointHistoryAudioService.createMultipleHistoryAudio(request);
//        return ResponseEntity.ok(ApiResponse.success("History audios created", null));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PointHistoryAudioResponse>> updateHistoryAudio(
            @PathVariable UUID pointId,
            @PathVariable UUID id,
            @Valid @RequestBody CreatePointHistoryAudio request) {
        request.setPointId(pointId);
        PointHistoryAudioResponse response = pointHistoryAudioService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("History audio updated successfully", response));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHistoryAudio(
            @PathVariable UUID pointId,
            @PathVariable UUID id) {
        pointHistoryAudioService.delete(pointId, id);
        return ResponseEntity.ok(ApiResponse.success("History audio deleted successfully", null));
    }

    @PostMapping("/translate")
    public ResponseEntity<ApiResponse<List<HistoryAudioTranslationObject>>> historyAudioAITranslate(
            @RequestBody HistoryAudioTranslateRequest request, @PathVariable String pointId) {
        List<HistoryAudioTranslationObject> res = pointHistoryAudioService.getTranslationFromAI(request);
        return ResponseEntity.ok(ApiResponse.success("History audio translated successfully", res));
    }

    @PostMapping(value = "/text-to-speech", produces = {"audio/mpeg", "application/json"})
    public ResponseEntity<Resource> getTextToSpeech(
            @RequestBody CreateSpeechFromTextRequest request) {

        Resource audioStream = pointHistoryAudioService.createSpeechFromText(request);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(audioStream);
    }

    @PostMapping(value = "/forced-alignment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ForcedAlignmentResponse> alignText(
            @RequestPart("file") MultipartFile file,
            @RequestPart("text") String text
    ) throws IOException {
        var alignment = pointHistoryAudioService.getForcedAlignment(file, text);
        return ResponseEntity.ok(alignment);
    }
}