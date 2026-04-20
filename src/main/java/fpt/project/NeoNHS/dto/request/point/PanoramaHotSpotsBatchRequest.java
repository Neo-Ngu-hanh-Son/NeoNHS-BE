package fpt.project.NeoNHS.dto.request.point;

import lombok.Data;

import java.util.List;

@Data
public class PanoramaHotSpotsBatchRequest {
    List<PanoramaHotSpotRequest> hotSpots;
}
