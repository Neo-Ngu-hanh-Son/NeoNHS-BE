package fpt.project.NeoNHS.dto.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
    private long total;
    private long active;
    private long banned;
    private long unverified;
    private long inactive;
}
