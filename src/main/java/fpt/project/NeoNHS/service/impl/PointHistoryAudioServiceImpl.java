package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.project.NeoNHS.constants.PointHistoryAudioConstants;
import fpt.project.NeoNHS.dto.request.point.AudioMetadataRequest;
import fpt.project.NeoNHS.dto.request.point.CreateMultiplePointHistoryAudioRequest;
import fpt.project.NeoNHS.dto.request.point.CreatePointHistoryAudio;
import fpt.project.NeoNHS.dto.request.point.WordTimingRequest;
import fpt.project.NeoNHS.dto.response.point.PointHistoryAudioResponse;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.entity.PointHistoryAudio;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.PointHistoryAudioRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.service.PointHistoryAudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointHistoryAudioServiceImpl implements PointHistoryAudioService {

    private final PointHistoryAudioRepository pointHistoryAudioRepository;
    private final PointRepository pointRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    // ─── PRIVATE HELPERS ──────────────────────────────────────────────────

    /**
     * Flatten metadata fields from the nested DTO into entity columns.
     * Handles null metadata gracefully (e.g. for "upload" mode).
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
