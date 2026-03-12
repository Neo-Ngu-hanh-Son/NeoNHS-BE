package fpt.project.NeoNHS.dto.response.checkin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCheckinResultResponse {
    private int earnedPoints;
    private int userTotalPoints;
}
