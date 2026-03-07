package fpt.project.NeoNHS.dto.request.point;

import lombok.Data;

@Data
public class AudioMetadataRequest {
    private String mode;
    private String modelId;
    private String voiceId;
    private String language;
    private String title;
    private String artist;
    private String coverImage;
}
