package fpt.project.NeoNHS.dto.request.point;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CheckinPointRequest {
    @NotNull(message = "Point ID is required")
    private UUID pointId;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;

    @Size(max = 255, message = "Position string must not exceed 255 characters")
    private String position;

    @Size(max = 255, message = "Thumbnail URL must not exceed 255 characters")
    private String thumbnailUrl;

    private Boolean isActive;

    @Size(max = 255, message = "QR Code string must not exceed 255 characters")
    private String qrCode;

    @Min(value = -180, message = "Longitude must be greater than or equal to -180")
    @Max(value = 180, message = "Longitude must be less than or equal to 180")
    private BigDecimal longitude;

    @Min(value = -90, message = "Latitude must be greater than or equal to -90")
    @Max(value = 90, message = "Latitude must be less than or equal to 90")
    private BigDecimal latitude;

    @Min(value = 0, message = "Reward points cannot be negative")
    private Integer rewardPoints;

    @Size(max = 2048, message = "Panorama image URL string must not exceed 2048 characters")
    private String panoramaImageUrl;

    private Double defaultYaw;
    private Double defaultPitch;
}
