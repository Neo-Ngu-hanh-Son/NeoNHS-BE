package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.point.PanoramaHotSpotRequest;
import fpt.project.NeoNHS.dto.request.point.PanoramaRequest;
import fpt.project.NeoNHS.dto.response.point.PanoramaHotSpotResponse;
import fpt.project.NeoNHS.dto.response.point.PointPanoramaResponse;
import fpt.project.NeoNHS.entity.CheckinPoint;
import fpt.project.NeoNHS.entity.PanoramaHotSpot;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.CheckinPointRepository;
import fpt.project.NeoNHS.repository.PanoramaHotSpotRepository;
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
  private final CheckinPointRepository checkinPointRepository;
  private final PanoramaHotSpotRepository hotSpotRepository;

  // ═══════════════════════════════════════════════════════
  // Point Panorama CRUD
  // ═══════════════════════════════════════════════════════

  @Override
  @Transactional
  public PointPanoramaResponse createOrUpdatePointPanorama(UUID pointId, PanoramaRequest request) {
    Point point = pointRepository.findById(pointId)
        .orElseThrow(() -> new ResourceNotFoundException("Point not found with id: " + pointId));

    // Update panorama config on the point
    point.setPanoramaImageUrl(request.getPanoramaImageUrl());
    point.setDefaultYaw(request.getDefaultYaw() != null ? request.getDefaultYaw() : 0.0);
    point.setDefaultPitch(request.getDefaultPitch() != null ? request.getDefaultPitch() : 0.0);

    // Replace all existing hot spots with the new ones
    point.getPanoramaHotSpots().clear();

    if (request.getHotSpots() != null) {
      for (int i = 0; i < request.getHotSpots().size(); i++) {
        PanoramaHotSpotRequest hsReq = request.getHotSpots().get(i);
        PanoramaHotSpot hotSpot = PanoramaHotSpot.builder()
            .point(point)
            .yaw(hsReq.getYaw())
            .pitch(hsReq.getPitch())
            .tooltip(hsReq.getTooltip())
            .title(hsReq.getTitle())
            .description(hsReq.getDescription())
            .imageUrl(hsReq.getImageUrl())
            .orderIndex(hsReq.getOrderIndex() != null ? hsReq.getOrderIndex() : i)
            .build();
        point.getPanoramaHotSpots().add(hotSpot);
      }
    }

    Point saved = pointRepository.save(point);
    return mapPointToPanoramaResponse(saved);
  }

  @Override
  public PointPanoramaResponse getPointPanorama(UUID pointId) {
    Point point = pointRepository.findById(pointId)
        .orElseThrow(() -> new ResourceNotFoundException("Point not found with id: " + pointId));
    return mapPointToPanoramaResponse(point);
  }

  @Override
  @Transactional
  public void deletePointPanorama(UUID pointId) {
    Point point = pointRepository.findById(pointId)
        .orElseThrow(() -> new ResourceNotFoundException("Point not found with id: " + pointId));

    point.setPanoramaImageUrl(null);
    point.setDefaultYaw(0.0);
    point.setDefaultPitch(0.0);
    point.getPanoramaHotSpots().clear();

    pointRepository.save(point);
  }

  // ═══════════════════════════════════════════════════════
  // CheckinPoint Panorama CRUD
  // ═══════════════════════════════════════════════════════

  @Override
  @Transactional
  public PointPanoramaResponse createOrUpdateCheckinPointPanorama(UUID checkinPointId, PanoramaRequest request) {
    CheckinPoint cp = checkinPointRepository.findById(checkinPointId)
        .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + checkinPointId));

    cp.setPanoramaImageUrl(request.getPanoramaImageUrl());
    cp.setDefaultYaw(request.getDefaultYaw() != null ? request.getDefaultYaw() : 0.0);
    cp.setDefaultPitch(request.getDefaultPitch() != null ? request.getDefaultPitch() : 0.0);

    // Replace all existing hot spots
    cp.getPanoramaHotSpots().clear();

    if (request.getHotSpots() != null) {
      for (int i = 0; i < request.getHotSpots().size(); i++) {
        PanoramaHotSpotRequest hsReq = request.getHotSpots().get(i);
        PanoramaHotSpot hotSpot = PanoramaHotSpot.builder()
            .checkinPoint(cp)
            .yaw(hsReq.getYaw())
            .pitch(hsReq.getPitch())
            .tooltip(hsReq.getTooltip())
            .title(hsReq.getTitle())
            .description(hsReq.getDescription())
            .imageUrl(hsReq.getImageUrl())
            .orderIndex(hsReq.getOrderIndex() != null ? hsReq.getOrderIndex() : i)
            .build();
        cp.getPanoramaHotSpots().add(hotSpot);
      }
    }

    CheckinPoint saved = checkinPointRepository.save(cp);
    return mapCheckinPointToPanoramaResponse(saved);
  }

  @Override
  public PointPanoramaResponse getCheckinPointPanorama(UUID checkinPointId) {
    CheckinPoint cp = checkinPointRepository.findById(checkinPointId)
        .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + checkinPointId));

    if (cp.getPanoramaImageUrl() == null || cp.getPanoramaImageUrl().isBlank()) {
      throw new BadRequestException("CheckinPoint does not have a panorama image configured");
    }

    return mapCheckinPointToPanoramaResponse(cp);
  }

  @Override
  @Transactional
  public void deleteCheckinPointPanorama(UUID checkinPointId) {
    CheckinPoint cp = checkinPointRepository.findById(checkinPointId)
        .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + checkinPointId));

    cp.setPanoramaImageUrl(null);
    cp.setDefaultYaw(0.0);
    cp.setDefaultPitch(0.0);
    cp.getPanoramaHotSpots().clear();

    checkinPointRepository.save(cp);
  }

  // ═══════════════════════════════════════════════════════
  // Single HotSpot CRUD
  // ═══════════════════════════════════════════════════════

  @Override
  @Transactional
  public PanoramaHotSpotResponse addHotSpotToPoint(UUID pointId, PanoramaHotSpotRequest request) {
    Point point = pointRepository.findById(pointId)
        .orElseThrow(() -> new ResourceNotFoundException("Point not found with id: " + pointId));

    if (point.getPanoramaImageUrl() == null || point.getPanoramaImageUrl().isBlank()) {
      throw new BadRequestException("Point does not have a panorama image configured. Set up panorama first.");
    }

    PanoramaHotSpot hotSpot = buildHotSpotFromRequest(request);
    hotSpot.setPoint(point);

    return mapHotSpotToResponse(hotSpotRepository.save(hotSpot));
  }

  @Override
  @Transactional
  public PanoramaHotSpotResponse addHotSpotToCheckinPoint(UUID checkinPointId, PanoramaHotSpotRequest request) {
    CheckinPoint cp = checkinPointRepository.findById(checkinPointId)
        .orElseThrow(() -> new ResourceNotFoundException("CheckinPoint not found with id: " + checkinPointId));

    if (cp.getPanoramaImageUrl() == null || cp.getPanoramaImageUrl().isBlank()) {
      throw new BadRequestException("CheckinPoint does not have a panorama image configured. Set up panorama first.");
    }

    PanoramaHotSpot hotSpot = buildHotSpotFromRequest(request);
    hotSpot.setCheckinPoint(cp);

    return mapHotSpotToResponse(hotSpotRepository.save(hotSpot));
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
  public List<PanoramaHotSpotResponse> getHotSpotsByPoint(UUID pointId) {
    if (!pointRepository.existsById(pointId)) {
      throw new ResourceNotFoundException("Point not found with id: " + pointId);
    }
    return hotSpotRepository.findByPointIdOrderByOrderIndexAsc(pointId)
        .stream()
        .map(this::mapHotSpotToResponse)
        .toList();
  }

  @Override
  public List<PanoramaHotSpotResponse> getHotSpotsByCheckinPoint(UUID checkinPointId) {
    if (!checkinPointRepository.existsById(checkinPointId)) {
      throw new ResourceNotFoundException("CheckinPoint not found with id: " + checkinPointId);
    }
    return hotSpotRepository.findByCheckinPointIdOrderByOrderIndexAsc(checkinPointId)
        .stream()
        .map(this::mapHotSpotToResponse)
        .toList();
  }

  // ═══════════════════════════════════════════════════════
  // Private mapping helpers
  // ═══════════════════════════════════════════════════════

  private PanoramaHotSpot buildHotSpotFromRequest(PanoramaHotSpotRequest request) {
    return PanoramaHotSpot.builder()
        .yaw(request.getYaw())
        .pitch(request.getPitch())
        .tooltip(request.getTooltip())
        .title(request.getTitle())
        .description(request.getDescription())
        .imageUrl(request.getImageUrl())
        .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
        .build();
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

  private PointPanoramaResponse mapPointToPanoramaResponse(Point point) {
    String address = point.getAttraction() != null ? point.getAttraction().getAddress() : "";
    return PointPanoramaResponse.builder()
        .id(point.getId().toString())
        .name(point.getName())
        .address(address != null ? address : "")
        .description(point.getDescription())
        .panoramaImageUrl(point.getPanoramaImageUrl())
        .thumbnailUrl(point.getThumbnailUrl())
        .defaultYaw(point.getDefaultYaw() != null ? point.getDefaultYaw() : 0.0)
        .defaultPitch(point.getDefaultPitch() != null ? point.getDefaultPitch() : 0.0)
        .hotSpots(mapHotSpotsToResponses(point.getPanoramaHotSpots()))
        .build();
  }

  private PointPanoramaResponse mapCheckinPointToPanoramaResponse(CheckinPoint cp) {
    // Traverse CheckinPoint -> Point -> Attraction for address
    String address = "";
    if (cp.getPoint() != null && cp.getPoint().getAttraction() != null) {
      address = cp.getPoint().getAttraction().getAddress();
    }
    return PointPanoramaResponse.builder()
        .id(cp.getId().toString())
        .name(cp.getName())
        .address(address != null ? address : "")
        .description(cp.getDescription())
        .panoramaImageUrl(cp.getPanoramaImageUrl())
        .thumbnailUrl(null) // CheckinPoint doesn't have a thumbnail
        .defaultYaw(cp.getDefaultYaw() != null ? cp.getDefaultYaw() : 0.0)
        .defaultPitch(cp.getDefaultPitch() != null ? cp.getDefaultPitch() : 0.0)
        .hotSpots(mapHotSpotsToResponses(cp.getPanoramaHotSpots()))
        .build();
  }
}
