package fpt.project.NeoNHS.dto.response.upload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageUploadResponse {
    private String mediaUrl;
    private String publicId;
}
