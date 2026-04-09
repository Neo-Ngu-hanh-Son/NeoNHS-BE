package fpt.project.NeoNHS.dto.response.checkin;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserCheckinResultResponse {
    private UUID checkinPointId;
    private int earnedPoints;
    private int userTotalPoints;
}
