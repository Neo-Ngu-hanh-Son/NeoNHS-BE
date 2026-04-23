package fpt.project.NeoNHS.dto.request.point;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import fpt.project.NeoNHS.enums.PointDifficulty;
import fpt.project.NeoNHS.enums.PointVibe;
import fpt.project.NeoNHS.enums.PointType;

@Data
public class PointRequest {
    private String name;
    private String description;
    private String thumbnailUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private Integer orderIndex;
    private Integer estTimeSpent;
    private PointType type;
    private UUID attractionId;
    private String googlePlaceId;
    private PointDifficulty difficulty;
    private PointVibe vibe;
}
