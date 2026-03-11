package fpt.project.NeoNHS.dto.request.point;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CheckinPointRequest {
    private UUID pointId;
    private String name;
    private String description;
    private String position;
    private String thumbnailUrl;
    private Boolean isActive;
    private String qrCode;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer rewardPoints;
    private String panoramaImageUrl;
    private Double defaultYaw;
    private Double defaultPitch;
}
