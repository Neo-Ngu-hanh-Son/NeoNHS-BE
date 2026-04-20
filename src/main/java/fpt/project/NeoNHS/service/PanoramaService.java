package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.point.PanoramaHotSpotRequest;
import fpt.project.NeoNHS.dto.request.point.PanoramaHotSpotsBatchRequest;
import fpt.project.NeoNHS.dto.request.point.PanoramaRequest;
import fpt.project.NeoNHS.dto.response.point.LinkingPanoramaResponse;
import fpt.project.NeoNHS.dto.response.point.PanoramaHotSpotResponse;
import fpt.project.NeoNHS.dto.response.point.PointPanoramaResponse;

import java.util.List;
import java.util.UUID;

public interface PanoramaService {

    // ─── Point Panorama CRUD ───
    PointPanoramaResponse addPanoramaToPoint(UUID pointId, PanoramaRequest request);

    PointPanoramaResponse updatePanorama(UUID panoramaId, PanoramaRequest request);

    PointPanoramaResponse getPanoramaById(UUID panoramaId);

    List<PointPanoramaResponse> getPanoramasByPoint(UUID pointId);

    void deletePanorama(UUID panoramaId);

    // ─── Single HotSpot CRUD ───
    PanoramaHotSpotResponse addHotSpotToPanorama(UUID panoramaId, PanoramaHotSpotRequest request);

    List<PanoramaHotSpotResponse> addHotSpotsToPanorama(UUID panoramaId, PanoramaHotSpotsBatchRequest request);

    PanoramaHotSpotResponse updateHotSpot(UUID hotSpotId, PanoramaHotSpotRequest request);

    void deleteHotSpot(UUID hotSpotId);

    List<PanoramaHotSpotResponse> getHotSpotsByPanorama(UUID panoramaId);

    List<LinkingPanoramaResponse> getAllPanoramaForLinking();
}
