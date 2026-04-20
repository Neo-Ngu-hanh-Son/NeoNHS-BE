package fpt.project.NeoNHS.dto.request.point;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanoramaRequest {
    @Size(min = 0, max = 120, message = "Title must be between 0 and 120 characters")
    private String title;
    @NotBlank(message = "Panorama image URL is required")
    private String panoramaImageUrl;
    private Double defaultYaw;
    private Double defaultPitch;
    private Boolean isDefault = false;
    @Valid
    private List<PanoramaHotSpotRequest> hotSpots;
}
