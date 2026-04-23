package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fpt.project.NeoNHS.config.OpenAiConfig;
import fpt.project.NeoNHS.constants.PointHistoryAudioConstants;
import fpt.project.NeoNHS.dto.request.point.historyAudio.*;
import fpt.project.NeoNHS.dto.request.point.CreateMultiplePointHistoryAudioRequest;
import fpt.project.NeoNHS.dto.request.point.CreatePointHistoryAudio;
import fpt.project.NeoNHS.dto.response.point.historyAudio.ForcedAlignmentResponse;
import fpt.project.NeoNHS.dto.response.point.historyAudio.HistoryAudioTranslationObject;
import fpt.project.NeoNHS.dto.response.point.historyAudio.PointHistoryAudioResponse;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.entity.PointHistoryAudio;
import fpt.project.NeoNHS.exception.AiResponseParseException;
import fpt.project.NeoNHS.exception.AiServiceUnavailableException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.PointHistoryAudioRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.service.PointHistoryAudioService;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointHistoryAudioServiceImpl implements PointHistoryAudioService {

    private final PointHistoryAudioRepository pointHistoryAudioRepository;
    private final PointRepository pointRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenAiConfig openAiConfig;
    private final RestClient openAiRestClient;
    private final RestClient elevenLabsRestClient;

    @Override
    @Transactional
    public PointHistoryAudioResponse create(CreatePointHistoryAudio request) {
        Point point = pointRepository.findById(request.getPointId())
                .orElseThrow(() -> new ResourceNotFoundException("Point", "id", request.getPointId()));

        String title = request.getMetadata() != null && request.getMetadata().getTitle() != null
                ? request.getMetadata().getTitle()
                : "Audio for Point " + point.getName();
        String artist = request.getMetadata() != null && request.getMetadata().getArtist() != null
                ? request.getMetadata().getArtist()
                : PointHistoryAudioConstants.DEFAULT_ARTIST;

        String coverImage = request.getMetadata() != null && request.getMetadata().getCoverImage() != null
                ? request.getMetadata().getCoverImage()
                : point.getThumbnailUrl();

        PointHistoryAudio entity = PointHistoryAudio.builder()
                .title(title)
                .artist(artist)
                .audioUrl(request.getAudioUrl())
                .historyText(request.getHistoryText())
                .words(serializeWords(request.getWords()))
                .point(point)
                .coverImage(coverImage)
                .build();

        mapMetadataToEntity(request, entity);

        PointHistoryAudio saved = pointHistoryAudioRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void createMultipleHistoryAudio(CreateMultiplePointHistoryAudioRequest request) {
        for (CreatePointHistoryAudio req : request.getPointHistoryAudios()) {
            create(req);
        }
    }

    @Override
    @Transactional
    public PointHistoryAudioResponse update(UUID id, CreatePointHistoryAudio request) {
        if (request.getPointId() != null && !checkIfPointExists(request.getPointId())) {
            throw new ResourceNotFoundException("Point", "id", request.getPointId());
        }
        PointHistoryAudio entity = pointHistoryAudioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PointHistoryAudio", "id", id));

        // In case point id change (Meaning copy from a point to another point)
//        if (request.getPointId() != null && !request.getPointId().equals(entity.getPoint().getId())) {
//            Point newPoint = pointRepository.findById(request.getPointId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Point", "id", request.getPointId()));
//            entity.setPoint(newPoint);
//        }


        entity.setAudioUrl(request.getAudioUrl());
        entity.setHistoryText(request.getHistoryText());
        entity.setWords(serializeWords(request.getWords()));

        mapMetadataToEntity(request, entity);

        PointHistoryAudio saved = pointHistoryAudioRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PointHistoryAudioResponse getByPointIdAndId(UUID pointId, UUID id) {
        if (pointId != null && !checkIfPointExists(pointId)) {
            throw new ResourceNotFoundException("Point", "id", pointId);
        }
        PointHistoryAudio entity = pointHistoryAudioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("PointHistoryAudio", "id", id));
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointHistoryAudioResponse> getAllByPointId(UUID pointId) {
        pointRepository.findById(pointId)
                .orElseThrow(() -> new ResourceNotFoundException("Point", "id", pointId));

        return pointHistoryAudioRepository.findByPointIdAndDeletedAtIsNull(pointId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID pointId, UUID id) {
        if (pointId != null && !checkIfPointExists(pointId)) {
            throw new ResourceNotFoundException("Point", "id", pointId);
        }
        PointHistoryAudio entity = pointHistoryAudioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("PointHistoryAudio", "id", id));
        entity.setDeletedAt(LocalDateTime.now());
        pointHistoryAudioRepository.save(entity);
    }

    @Override
    public List<HistoryAudioTranslationObject> getTranslationFromAI(HistoryAudioTranslateRequest request) {
        try {
            ObjectNode gptRequest = objectMapper.createObjectNode();
            StringBuilder message = new StringBuilder(PointHistoryAudioConstants.DEFAULT_TRANSLATION_PROMPT);

            String requestJson = objectMapper.writeValueAsString(request);
            message.append(" User prompt: ").append(requestJson);

            ArrayNode messagesArray = objectMapper.createArrayNode();
            ObjectNode userMessage = objectMapper.createObjectNode();

            gptRequest.put("model", openAiConfig.getModel());
            userMessage.put("role", "user");
            userMessage.put("content", message.toString());

            messagesArray.add(userMessage);
            gptRequest.set("messages", messagesArray);

            gptRequest.put("temperature", 0.3);
            gptRequest.put("max_tokens", 4096);

            String response = openAiRestClient.post()
                    .uri(openAiConfig.getChatCompletionUrl())
                    .body(gptRequest.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(response);
            String aiResultText = rootNode.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            System.out.println("AI result: " + aiResultText);
            return objectMapper.readValue(
                    aiResultText,
                    new TypeReference<>() {
                    }
            );

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response JSON", e);
            throw new AiResponseParseException("Failed to parse AI response, please try again", e);
        } catch (AiServiceUnavailableException e) {
            log.error("Connect to AI Service failed", e);
            throw new AiServiceUnavailableException("Connect to AI Service failed");
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while processing AI translation", e);
        }
    }

    @Override
    public Resource createSpeechFromText(CreateSpeechFromTextRequest request) {
        ElevenLabRequest elevenLabRequest = ElevenLabRequest.builder()
//                .modelId(request.getModelId())
//                .modelId("eleven_turbo_v2_5")
                .modelId("eleven_v3")
//                .languageCode(request.getLanguageCode())
                .text(request.getText())
                .voiceSettings(ElevenLabVoiceSettings.builder()
                        .speed(0.91)
                        .stability(0.60)
                        .similarityBoost(0.75)
                        .useSpeakerBoost(true)
                        .build())
                .applyTextNormalization("auto")
//                .applyLanguageTextNormalization(request.getLanguageCode().equals("ja"))
                .build();
        System.out.println("ElevenLabRequest: " + elevenLabRequest.toString());
        Resource audioData = elevenLabsRestClient.post()
                .uri("/text-to-speech/" + request.getVoiceId() + "?output_format=" + request.getOutputFormat())
                .body(elevenLabRequest)
                .retrieve()
                .onStatus(status -> status.value() == 422, (req, res) -> {
                    String errorBody = new String(res.getBody().readAllBytes());
                    log.error("ElevenLabs Validation Error: {}", errorBody);
                    throw new BadRequestException("Bad request: " + errorBody, null);
                })
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    System.err.println("Status Code: " + res.getStatusCode());
                    String errorBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    System.err.println("ElevenLabs Error Body: " + errorBody);
                    throw new AiServiceUnavailableException("AI voice generation service is not available: ");
                })
                .body(Resource.class);
        return audioData;
    }

    @Override
    public ForcedAlignmentResponse getForcedAlignment(MultipartFile audioFile, String text) throws IOException {
        // 1. Convert MultipartFile to a custom ByteArrayResource
        Resource audioResource = new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                String originalName = audioFile.getOriginalFilename();
                return (originalName != null && !originalName.isEmpty()) ? originalName : "audio.mp3";
            }
        };

        // 2. Prepare the multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", audioResource);
        body.add("text", text);

        // 2. Make the call and map directly to our DTO
        return elevenLabsRestClient.post()
                .uri("/forced-alignment")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String error = new String(res.getBody().readAllBytes());
                    log.error("Alignment failed: {}", error);
                    throw new AiServiceUnavailableException("ElevenLabs Alignment Error: " + error);
                })
                .body(ForcedAlignmentResponse.class);
    }

    /**
     * Flatten metadata fields from the nested DTO into entity columns.
     */
    private void mapMetadataToEntity(CreatePointHistoryAudio request, PointHistoryAudio entity) {
        if (request.getMetadata() != null) {
            entity.setMode(request.getMetadata().getMode());
            entity.setLanguage(request.getMetadata().getLanguage());
            entity.setModelId(request.getMetadata().getModelId());
            entity.setVoiceId(request.getMetadata().getVoiceId());
            entity.setTitle(request.getMetadata().getTitle());
            entity.setArtist(request.getMetadata().getArtist());
            entity.setCoverImage(request.getMetadata().getCoverImage());
        } else {
            entity.setMode(null);
            entity.setLanguage(null);
            entity.setModelId(null);
            entity.setVoiceId(null);
            entity.setTitle(null);
            entity.setArtist(null);
            entity.setCoverImage(null);
        }
    }

    /**
     * Serialize List<WordTimingRequest> into JSON String to store it.
     */
    private String serializeWords(List<WordTimingRequest> words) {
        if (words == null || words.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(words);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize word timings to JSON", e);
            throw new RuntimeException("Failed to serialize word timings", e);
        }
    }

    /**
     * Deserialize the words JSON column back into a list.
     */
    private List<WordTimingRequest> deserializeWords(String wordsJson) {
        if (wordsJson == null || wordsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(wordsJson, new TypeReference<List<WordTimingRequest>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize word timings from JSON", e);
            throw new RuntimeException("Failed to deserialize word timings", e);
        }
    }

    /**
     * Convert entity to response DTO, unflattening metadata fields
     * and deserializing the words JSON.
     */
    private PointHistoryAudioResponse toResponse(PointHistoryAudio entity) {
        AudioMetadataRequest metadata = new AudioMetadataRequest();
        metadata.setMode(entity.getMode());
        metadata.setModelId(entity.getModelId());
        metadata.setVoiceId(entity.getVoiceId());
        metadata.setLanguage(entity.getLanguage());
        metadata.setTitle(entity.getTitle());
        metadata.setArtist(entity.getArtist());
        metadata.setCoverImage(entity.getCoverImage());

        return PointHistoryAudioResponse.builder()
                .id(entity.getId())
                .pointId(entity.getPoint().getId())
                .audioUrl(entity.getAudioUrl())
                .historyText(entity.getHistoryText())
                .words(deserializeWords(entity.getWords()))
                .metadata(metadata)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private boolean checkIfPointExists(UUID pointId) {
        return pointRepository.existsByIdAndDeletedAtIsNull(pointId);
    }
}
