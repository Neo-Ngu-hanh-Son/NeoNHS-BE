package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.point.CreateMultiplePointHistoryAudioRequest;
import fpt.project.NeoNHS.dto.request.point.CreatePointHistoryAudio;
import fpt.project.NeoNHS.dto.request.point.historyAudio.HistoryAudioTranslateRequest;
import fpt.project.NeoNHS.dto.response.point.historyAudio.GeminiTranslationObject;
import fpt.project.NeoNHS.dto.response.point.historyAudio.PointHistoryAudioResponse;

import java.util.List;
import java.util.UUID;

public interface PointHistoryAudioService {

    PointHistoryAudioResponse create(CreatePointHistoryAudio request);

    void createMultipleHistoryAudio(CreateMultiplePointHistoryAudioRequest request);

    PointHistoryAudioResponse update(UUID id, CreatePointHistoryAudio request);

    PointHistoryAudioResponse getByPointIdAndId(UUID pointId, UUID id);

    List<PointHistoryAudioResponse> getAllByPointId(UUID pointId);

    void delete(UUID pointId, UUID id);

    List<GeminiTranslationObject> getTranslateFromGemini(HistoryAudioTranslateRequest request);
}
