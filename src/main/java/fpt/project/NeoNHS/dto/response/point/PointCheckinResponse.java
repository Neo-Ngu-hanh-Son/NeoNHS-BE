package fpt.project.NeoNHS.dto.response.point;

import fpt.project.NeoNHS.dto.response.auth.UserInfoResponse;
import fpt.project.NeoNHS.dto.response.blog.BlogCategoryResponse;
import fpt.project.NeoNHS.dto.response.blog.BlogResponse;
import fpt.project.NeoNHS.entity.Blog;
import fpt.project.NeoNHS.entity.CheckinPoint;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PointCheckinResponse {
    private UUID id;
    private String name;
    private String description;
    private String position;
    private String thumbnailUrl;
    private Boolean isActive;
    private String qrCode;
    private Double longitude;
    private Double latitude;
    private Integer rewardPoints;
    private String panoramaImageUrl;
    private Double defaultYaw;
    private Double defaultPitch;

    private Boolean isUserCheckedIn; // Indicates if the user has checked in at this point
    private String parentPointId;
    private String parentPointName;

    /**
     * Factory method to create a PointCheckinResponse from a CheckinPoint entity and user's check-in status. <br/>
     * NOTE: The isUserCheckedIn parameter should be determined by the service layer based on the user's check-in history.
     * @param checkinPoint
     * @param isUserCheckedIn
     * @return
     */
    public static PointCheckinResponse fromEntity(CheckinPoint checkinPoint, Boolean isUserCheckedIn) {
        if (isUserCheckedIn == null) {
            isUserCheckedIn = false;
        }
        return PointCheckinResponse.builder()
                .id(checkinPoint.getId())
                .name(checkinPoint.getName())
                .description(checkinPoint.getDescription())
                .position(checkinPoint.getPosition())
                .thumbnailUrl(checkinPoint.getThumbnailUrl())
                .isActive(checkinPoint.getIsActive())
                .qrCode(checkinPoint.getQrCode())
                .longitude(checkinPoint.getLongitude().doubleValue())
                .latitude(checkinPoint.getLatitude().doubleValue())
                .rewardPoints(checkinPoint.getRewardPoints())
                .panoramaImageUrl(checkinPoint.getPanoramaImageUrl())
                .defaultYaw(checkinPoint.getDefaultYaw())
                .defaultPitch(checkinPoint.getDefaultPitch())
                .isUserCheckedIn(isUserCheckedIn)
                .parentPointId(checkinPoint.getPoint() != null ? checkinPoint.getPoint().getId().toString() : null)
                .parentPointName(checkinPoint.getPoint() != null ? checkinPoint.getPoint().getName() : null)
                .build();
    }
}
