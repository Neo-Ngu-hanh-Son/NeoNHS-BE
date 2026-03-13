package fpt.project.NeoNHS.dto.response.usercheckin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCheckinGalleryItemResponse {
    private UUID id;
    private String imageUrl;
    private String caption;
    private LocalDateTime takenAt;
    private UUID destinationId;
    private String destinationName;
    private UUID parentPointId;
    private String parentPointName;
    private UUID checkinPointId;
    private String checkinPointName;
}
