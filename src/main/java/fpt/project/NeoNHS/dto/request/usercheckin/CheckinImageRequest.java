package fpt.project.NeoNHS.dto.request.usercheckin;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckinImageRequest {
    private UUID id;
    private String imageUrl; // Front end only send image URLs (which is uploaded separately)
    private String caption;
}
