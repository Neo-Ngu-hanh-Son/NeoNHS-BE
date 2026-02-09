package fpt.project.NeoNHS.dto.request.point;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

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
    private String type;
    private UUID attractionId;
}
