package fpt.project.NeoNHS.dto.response.usercheckin;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CheckinImageResponse {
    private UUID id;
    private String imageUrl;
    private String caption;
}
