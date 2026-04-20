package fpt.project.NeoNHS.dto.response.point;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanoramaHotSpotResponse {
    private String id;
    private Double yaw;
    private Double pitch;
    private String tooltip;
    private String title;
    private String description;
    private String imageUrl;
    private Integer orderIndex;
    private String type;
    private String targetPanoramaId;
}
