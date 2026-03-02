package fpt.project.NeoNHS.dto.request.point;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanoramaRequest {

  @NotBlank(message = "Panorama image URL is required")
  private String panoramaImageUrl;

  private Double defaultYaw;

  private Double defaultPitch;

  @Valid
  private List<PanoramaHotSpotRequest> hotSpots;
}
