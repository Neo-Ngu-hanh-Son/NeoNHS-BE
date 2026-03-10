package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.point.PanoramaHotSpotRequest;
import fpt.project.NeoNHS.dto.request.point.PanoramaRequest;
import fpt.project.NeoNHS.dto.response.point.PanoramaHotSpotResponse;
import fpt.project.NeoNHS.dto.response.point.PointPanoramaResponse;

import java.util.List;
import java.util.UUID;

public interface PanoramaService {

  // ─── Point Panorama CRUD ───

  /**
   * Create or fully replace the panorama config (image + camera + hot spots) for
   * a Point
   */
  PointPanoramaResponse createOrUpdatePointPanorama(UUID pointId, PanoramaRequest request);

  /**
   * Get panorama data for a Point (public-facing, already exists in PointService
   * — kept here for admin too)
   */
  PointPanoramaResponse getPointPanorama(UUID pointId);

  /** Remove the entire panorama (image url + hot spots) from a Point */
  void deletePointPanorama(UUID pointId);

  // ─── CheckinPoint Panorama CRUD ───

  /** Create or fully replace the panorama config for a CheckinPoint */
  PointPanoramaResponse createOrUpdateCheckinPointPanorama(UUID checkinPointId, PanoramaRequest request);

  /** Get panorama data for a CheckinPoint */
  PointPanoramaResponse getCheckinPointPanorama(UUID checkinPointId);

  /** Remove the entire panorama from a CheckinPoint */
  void deleteCheckinPointPanorama(UUID checkinPointId);

  // ─── Single HotSpot CRUD ───

  /** Add a single hot spot to a Point's panorama */
  PanoramaHotSpotResponse addHotSpotToPoint(UUID pointId, PanoramaHotSpotRequest request);

  /** Add a single hot spot to a CheckinPoint's panorama */
  PanoramaHotSpotResponse addHotSpotToCheckinPoint(UUID checkinPointId, PanoramaHotSpotRequest request);

  /** Update an existing hot spot */
  PanoramaHotSpotResponse updateHotSpot(UUID hotSpotId, PanoramaHotSpotRequest request);

  /** Delete a single hot spot */
  void deleteHotSpot(UUID hotSpotId);

  /** Get all hot spots for a Point */
  List<PanoramaHotSpotResponse> getHotSpotsByPoint(UUID pointId);

  /** Get all hot spots for a CheckinPoint */
  List<PanoramaHotSpotResponse> getHotSpotsByCheckinPoint(UUID checkinPointId);
}
