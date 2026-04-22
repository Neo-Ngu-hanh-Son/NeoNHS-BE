package fpt.project.NeoNHS.dto.request.point.historyAudio;

import lombok.Data;

@Data
public class CreateSpeechFromTextRequest {
    private String voiceId;
    private String text;
    private String modelId;
    private String outputFormat;
//    private String languageCode;
}
