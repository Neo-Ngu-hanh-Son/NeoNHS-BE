package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.point.PanoramaHotSpotRequest;
import fpt.project.NeoNHS.dto.request.point.PanoramaHotSpotsBatchRequest;
import fpt.project.NeoNHS.dto.request.point.PanoramaRequest;
import fpt.project.NeoNHS.dto.response.point.LinkingPanoramaResponse;
import fpt.project.NeoNHS.dto.response.point.PanoramaHotSpotResponse;
import fpt.project.NeoNHS.dto.response.point.PointPanoramaResponse;
import fpt.project.NeoNHS.entity.PanoramaHotSpot;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.entity.PointPanorama;
import fpt.project.NeoNHS.enums.PanoramaHotSpotType;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.PanoramaHotSpotRepository;
import fpt.project.NeoNHS.repository.PointPanoramaRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.service.PanoramaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PanoramaServiceImpl implements PanoramaService {

    private final PointRepository pointRepository;
    private final PointPanoramaRepository pointPanoramaRepository;
    private final PanoramaHotSpotRepository hotSpotRepository;

    @Override
    @Transactional
    public PointPanoramaResponse addPanoramaToPoint(UUID pointId, PanoramaRequest request) {
        Point point = pointRepository.findById(pointId)
                .orElseThrow(() -> new ResourceNotFoundException("Point not found with id: " + pointId));

        boolean isFirst = point.getPanoramas() == null || point.getPanoramas().isEmpty();
        boolean shouldBeDefault = isFirst || request.getIsDefault() == true;

        if (shouldBeDefault && !isFirst) {
            // Update others to false
            for (PointPanorama p : point.getPanoramas()) {
                p.setIsDefault(false);
            }
        }

        PointPanorama panorama = PointPanorama.builder()
                .point(point)
                .title(request.getTitle())
                .panoramaImageUrl(request.getPanoramaImageUrl())
                .defaultYaw(request.getDefaultYaw() != null ? request.getDefaultYaw() : 0.0)
                .defaultPitch(request.getDefaultPitch() != null ? request.getDefaultPitch() : 0.0)
                .isDefault(shouldBeDefault)
                .build();

        panorama = pointPanoramaRepository.save(panorama);
        addHotSpotsToPanorama(request, panorama);
        return mapPointPanoramaToResponse(pointPanoramaRepository.save(panorama));
    }

    private void addHotSpotsToPanorama(PanoramaRequest request, PointPanorama panorama) {
        if (request.getHotSpots() != null) {
            for (int i = 0; i < request.getHotSpots().size(); i++) {
                PanoramaHotSpotRequest hsReq = request.getHotSpots().get(i);
                PanoramaHotSpot hotSpot = buildHotSpotFromRequest(hsReq, panorama.getId());
                hotSpot.setPointPanorama(panorama);
                hotSpot.setOrderIndex(hsReq.getOrderIndex() != null ? hsReq.getOrderIndex() : i);
                panorama.getPanoramaHotSpots().add(hotSpot);

            }
        }
    }

    @Override
    @Transactional
    public PointPanoramaResponse updatePanorama(UUID panoramaId, PanoramaRequest request) {
        PointPanorama panorama = pointPanoramaRepository.findById(panoramaId)
                .orElseThrow(() -> new ResourceNotFoundException("Panorama not found with id: " + panoramaId));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            pointPanoramaRepository.findByIsDefaultAndPoint_Id(true, panorama.getPoint().getId())
                    .ifPresent(existingDefault -> {
                        if (!existingDefault.getId().equals(panorama.getId())) {
                            existingDefault.setIsDefault(false);
                            pointPanoramaRepository.save(existingDefault);
                        }
                    });
            panorama.setIsDefault(true);
        }

        if (request.getTitle() != null)
            panorama.setTitle(request.getTitle());
        if (request.getPanoramaImageUrl() != null)
            panorama.setPanoramaImageUrl(request.getPanoramaImageUrl());
        if (request.getDefaultYaw() != null)
            panorama.setDefaultYaw(request.getDefaultYaw());
        if (request.getDefaultPitch() != null)
            panorama.setDefaultPitch(request.getDefaultPitch());

        panorama.getPanoramaHotSpots().clear();

        addHotSpotsToPanorama(request, panorama);
        return mapPointPanoramaToResponse(pointPanoramaRepository.save(panorama));
    }

    @Override
    public PointPanoramaResponse getPanoramaById(UUID panoramaId) {
        PointPanorama panorama = pointPanoramaRepository.findById(panoramaId)
                .orElseThrow(() -> new ResourceNotFoundException("Panorama not found with id: " + panoramaId));
        return mapPointPanoramaToResponse(panorama);
    }

    @Override
    public List<PointPanoramaResponse> getPanoramasByPoint(UUID pointId) {
        List<PointPanorama> panoramas = pointPanoramaRepository.findByPointId(pointId);
        return panoramas.stream().map(this::mapPointPanoramaToResponse).toList();
    }

    @Override
    @Transactional
    public void deletePanorama(UUID panoramaId) {
        PointPanorama panorama = pointPanoramaRepository.findById(panoramaId)
                .orElseThrow(() -> new ResourceNotFoundException("Panorama not found with id: " + panoramaId));
        boolean wasDefault = Boolean.TRUE.equals(panorama.getIsDefault());
        Point point = panorama.getPoint();

        pointPanoramaRepository.delete(panorama);

        if (wasDefault) {
            point.getPanoramas().remove(panorama);
            if (!point.getPanoramas().isEmpty()) {
                point.getPanoramas().getFirst().setIsDefault(true);
                pointRepository.save(point);
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // Single HotSpot CRUD
    // ═══════════════════════════════════════════════════════

    @Override
    @Transactional
    public PanoramaHotSpotResponse addHotSpotToPanorama(UUID panoramaId, PanoramaHotSpotRequest request) {
        PointPanorama panorama = pointPanoramaRepository.findById(panoramaId)
                .orElseThrow(() -> new ResourceNotFoundException("Panorama not found with id: " + panoramaId));

        PanoramaHotSpot hotSpot = buildHotSpotFromRequest(request, panorama.getId());
        hotSpot.setPointPanorama(panorama);

        return mapHotSpotToResponse(hotSpotRepository.save(hotSpot));
    }

    @Override
    public List<PanoramaHotSpotResponse> addHotSpotsToPanorama(UUID panoramaId, PanoramaHotSpotsBatchRequest request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Transactional
    public PanoramaHotSpotResponse updateHotSpot(UUID hotSpotId, PanoramaHotSpotRequest request) {
        PanoramaHotSpot hotSpot = hotSpotRepository.findById(hotSpotId)
                .orElseThrow(() -> new ResourceNotFoundException("HotSpot not found with id: " + hotSpotId));

        if (request.getYaw() != null)
            hotSpot.setYaw(request.getYaw());
        if (request.getPitch() != null)
            hotSpot.setPitch(request.getPitch());
        if (request.getTooltip() != null)
            hotSpot.setTooltip(request.getTooltip());
        if (request.getTitle() != null)
            hotSpot.setTitle(request.getTitle());
        if (request.getDescription() != null)
            hotSpot.setDescription(request.getDescription());
        if (request.getImageUrl() != null)
            hotSpot.setImageUrl(request.getImageUrl());
        if (request.getOrderIndex() != null)
            hotSpot.setOrderIndex(request.getOrderIndex());

        var requestType = request.getType() != null ? PanoramaHotSpotType.valueOf(request.getType()) : PanoramaHotSpotType.INFO;
        hotSpot.setType(PanoramaHotSpotType.valueOf(request.getType()));

        if (requestType == PanoramaHotSpotType.LINK && request.getTargetPanoramaId() != null) {
            PointPanorama target = pointPanoramaRepository.findById(request.getTargetPanoramaId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Target panorama not found: " + request.getTargetPanoramaId()));
            hotSpot.setTargetPanorama(target);
        } else {
            hotSpot.setTargetPanorama(null);
        }

        return mapHotSpotToResponse(hotSpotRepository.save(hotSpot));
    }

    @Override
    @Transactional
    public void deleteHotSpot(UUID hotSpotId) {
        if (!hotSpotRepository.existsById(hotSpotId)) {
            throw new ResourceNotFoundException("HotSpot not found with id: " + hotSpotId);
        }
        hotSpotRepository.deleteById(hotSpotId);
    }

    @Override
    public List<PanoramaHotSpotResponse> getHotSpotsByPanorama(UUID panoramaId) {
        if (!pointPanoramaRepository.existsById(panoramaId)) {
            throw new ResourceNotFoundException("Panorama not found with id: " + panoramaId);
        }
        return hotSpotRepository.findByPointPanoramaIdOrderByOrderIndexAsc(panoramaId)
                .stream()
                .map(this::mapHotSpotToResponse)
                .toList();
    }

    // ═══════════════════════════════════════════════════════
    // Private mapping helpers
    // ═══════════════════════════════════════════════════════

    private PanoramaHotSpot buildHotSpotFromRequest(PanoramaHotSpotRequest request, UUID currentPanoramaId) {
        if (currentPanoramaId.equals(request.getTargetPanoramaId())) {
            throw new IllegalArgumentException("A panorama cannot link to itself.");
        }
        PanoramaHotSpot hotSpot = PanoramaHotSpot.builder()
                .yaw(request.getYaw())
                .pitch(request.getPitch())
                .tooltip(request.getTooltip() == null ? request.getTitle() : request.getTooltip())
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .type(request.getType() != null ? PanoramaHotSpotType.valueOf(request.getType()) : PanoramaHotSpotType.INFO)
                .build();

        if (request.getTargetPanoramaId() != null && hotSpot.getType() == PanoramaHotSpotType.LINK) {
            PointPanorama target = pointPanoramaRepository.findById(request.getTargetPanoramaId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Target panorama not found: " + request.getTargetPanoramaId()));
            hotSpot.setTargetPanorama(target);
        }
        return hotSpot;
    }

    private PanoramaHotSpotResponse mapHotSpotToResponse(PanoramaHotSpot entity) {
        return PanoramaHotSpotResponse.builder()
                .id(entity.getId().toString())
                .yaw(entity.getYaw())
                .pitch(entity.getPitch())
                .tooltip(entity.getTooltip())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .orderIndex(entity.getOrderIndex())
                .type(entity.getType() != null ? entity.getType().name() : null)
                .targetPanoramaId(entity.getTargetPanorama() != null ? entity.getTargetPanorama().getId().toString() : null)
                .build();
    }

    private List<PanoramaHotSpotResponse> mapHotSpotsToResponses(List<PanoramaHotSpot> hotSpots) {
        if (hotSpots == null)
            return new ArrayList<>();
        return hotSpots.stream()
                .sorted(Comparator.comparingInt(m -> m.getOrderIndex() != null ? m.getOrderIndex() : 0))
                .map(this::mapHotSpotToResponse)
                .toList();
    }

    private PointPanoramaResponse mapPointPanoramaToResponse(PointPanorama panorama) {
        return PointPanoramaResponse.builder()
                .id(panorama.getId().toString())
                .title(panorama.getTitle())
                .panoramaImageUrl(panorama.getPanoramaImageUrl())
                .defaultYaw(panorama.getDefaultYaw() != null ? panorama.getDefaultYaw() : 0.0)
                .defaultPitch(panorama.getDefaultPitch() != null ? panorama.getDefaultPitch() : 0.0)
                .isDefault(panorama.getIsDefault())
                .placeId(panorama.getPoint().getId().toString())
                .hotSpots(mapHotSpotsToResponses(panorama.getPanoramaHotSpots()))
                .build();
    }

    @Override
    public List<LinkingPanoramaResponse> getAllPanoramaForLinking() {
        // Fetch all point info along with their panorama
        var pointList = pointPanoramaRepository.getAllPanoramaForLinking();
        List<LinkingPanoramaResponse> list = pointList.stream().map(LinkingPanoramaResponse::fromEntity).toList();
        return list;
    }
}
