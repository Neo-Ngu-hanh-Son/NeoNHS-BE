package fpt.project.NeoNHS.dto.request.point;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import fpt.project.NeoNHS.enums.PointType;

@Data
public class PointRequest {
    private String name;
    private String description;
    private String thumbnailUrl;
    private String history;
    private String historyAudioUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer orderIndex;
    private Integer estTimeSpent;
    private PointType type;
    private UUID attractionId;
}
