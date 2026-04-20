package fpt.project.NeoNHS.dto.response.point;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointPanoramaResponse {
  private String id;
  private String title;
  private String panoramaImageUrl;
  private Double defaultYaw;
  private Double defaultPitch;
  private Boolean isDefault;
  private String placeId;
  private List<PanoramaHotSpotResponse> hotSpots;
}
