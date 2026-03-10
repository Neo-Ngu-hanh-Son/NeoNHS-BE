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
  private String name;
  private String address;
  private String description;
  private String panoramaImageUrl;
  private String thumbnailUrl;
  private Double defaultYaw;
  private Double defaultPitch;
  private List<PanoramaHotSpotResponse> hotSpots;
}
