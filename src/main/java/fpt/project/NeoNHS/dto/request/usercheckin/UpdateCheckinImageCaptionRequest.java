package fpt.project.NeoNHS.dto.request.usercheckin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateCheckinImageCaptionRequest {
    @NotNull(message = "Image ID is required")
    private UUID imageId;
    
    private String caption;
}
