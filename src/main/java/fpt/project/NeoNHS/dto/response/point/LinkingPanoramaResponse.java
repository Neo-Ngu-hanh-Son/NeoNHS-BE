package fpt.project.NeoNHS.dto.response.point;

import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.entity.PointPanorama;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

// Simple wrapper for linking purpose only
@Builder
@Data
public class LinkingPanoramaResponse {
    private String pointName;
    private String pointId;
    private List<PointPanoramaResponse> panoramas;

    public static LinkingPanoramaResponse fromEntity(Point p) {
        if (p == null) return null;
        return LinkingPanoramaResponse.builder()
                .pointName(p.getName())
                .pointId(p.getId() != null ? p.getId().toString() : null)
                .panoramas(p.getPanoramas() == null ? List.of() : p.getPanoramas().stream()
                        .map(pano -> PointPanoramaResponse.builder()
                                .id(pano.getId().toString())
                                .title(pano.getTitle())
                                .panoramaImageUrl(pano.getPanoramaImageUrl())
                                .build())
                        .toList())
                .build();
    }
}
