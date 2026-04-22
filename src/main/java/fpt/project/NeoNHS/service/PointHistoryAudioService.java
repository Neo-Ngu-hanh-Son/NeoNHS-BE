package fpt.project.NeoNHS.service;

import com.fasterxml.jackson.databind.JsonNode;
import fpt.project.NeoNHS.dto.request.point.CreateMultiplePointHistoryAudioRequest;
import fpt.project.NeoNHS.dto.request.point.CreatePointHistoryAudio;
import fpt.project.NeoNHS.dto.request.point.historyAudio.CreateSpeechFromTextRequest;
import fpt.project.NeoNHS.dto.request.point.historyAudio.HistoryAudioTranslateRequest;
import fpt.project.NeoNHS.dto.response.point.historyAudio.ForcedAlignmentResponse;
import fpt.project.NeoNHS.dto.response.point.historyAudio.HistoryAudioTranslationObject;
import fpt.project.NeoNHS.dto.response.point.historyAudio.PointHistoryAudioResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface PointHistoryAudioService {

    PointHistoryAudioResponse create(CreatePointHistoryAudio request);

    void createMultipleHistoryAudio(CreateMultiplePointHistoryAudioRequest request);

    PointHistoryAudioResponse update(UUID id, CreatePointHistoryAudio request);

    PointHistoryAudioResponse getByPointIdAndId(UUID pointId, UUID id);

    List<PointHistoryAudioResponse> getAllByPointId(UUID pointId);

    void delete(UUID pointId, UUID id);

    List<HistoryAudioTranslationObject> getTranslationFromAI(HistoryAudioTranslateRequest request);

    Resource createSpeechFromText(CreateSpeechFromTextRequest request);

    ForcedAlignmentResponse getForcedAlignment(MultipartFile resource, String text) throws IOException;
}
