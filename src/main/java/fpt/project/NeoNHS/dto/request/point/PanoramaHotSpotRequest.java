package fpt.project.NeoNHS.dto.request.point;

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

  @NotBlank(message = "Tooltip is required")
  @Size(max = 100)
  private String tooltip;

  @NotBlank(message = "Title is required")
  @Size(max = 255)
  private String title;

  @NotBlank(message = "Description is required")
  private String description;

  private String imageUrl;

  private Integer orderIndex;
}
