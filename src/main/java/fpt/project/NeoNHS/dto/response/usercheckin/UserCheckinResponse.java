package fpt.project.NeoNHS.dto.response.usercheckin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserCheckinResponse {
    private UUID id;
    private LocalDateTime checkinTime;
    private String checkinMethod;
    private String note;
    private Integer earnedPoints;
    private UUID checkinPointId;
    private List<CheckinImageResponse> images;
}
