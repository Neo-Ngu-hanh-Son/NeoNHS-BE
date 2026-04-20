package fpt.project.NeoNHS.dto.request.point;

import java.util.UUID;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanoramaHotSpotRequest {

  @NotNull(message = "Yaw is required")
  private Double yaw;

  @NotNull(message = "Pitch is required")
  private Double pitch;

  @Size(max = 100)
  private String tooltip;

  @Size(max = 255)
  private String title;

  private String description;

  private String imageUrl;

  private Integer orderIndex;

  @NotBlank(message = "Type is required (INFO / LINK)")
  private String type;

  private UUID targetPanoramaId;
}
