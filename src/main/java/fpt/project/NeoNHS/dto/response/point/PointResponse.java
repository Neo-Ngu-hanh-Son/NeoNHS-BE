package fpt.project.NeoNHS.dto.response.point;

import jakarta.persistence.Column;
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
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer orderIndex;
    private Integer estTimeSpent;
    private PointType type;
    private UUID attractionId;
    private String panoramaImageUrl;
    @Builder.Default
    private Double defaultYaw = 0.0;
    @Builder.Default
    private Double defaultPitch = 0.0;
    private Integer historyAudioCount;
}
