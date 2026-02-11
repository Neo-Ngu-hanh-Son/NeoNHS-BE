package fpt.project.NeoNHS.dto.response.point;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

import fpt.project.NeoNHS.enums.PointType;

@Data
@Builder
public class PointResponse {
    private UUID id;
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
