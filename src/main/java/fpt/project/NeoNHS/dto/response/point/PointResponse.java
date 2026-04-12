package fpt.project.NeoNHS.dto.response.point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

import fpt.project.NeoNHS.enums.PointType;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Response DTO for Point entity, used to send point details to clients.
 * NOTE that both workshops and events will also be represented as points, so
 * the type field is used to distinguish between them.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PointResponse {
    protected UUID id;
    protected String name;
    protected String description;
    protected String thumbnailUrl;
    protected Double latitude;
    protected Double longitude;
    protected Integer orderIndex;
    protected Integer estTimeSpent;
    protected PointType type;
    protected UUID attractionId;
    protected String panoramaImageUrl;
    @Builder.Default
    protected Double defaultYaw = 0.0;
    @Builder.Default
    protected Double defaultPitch = 0.0;
    protected String googlePlaceId;
    protected Integer historyAudioCount;
    private List<CheckinPointResponse> checkinPoints;
    protected java.time.LocalDateTime deletedAt;
}
