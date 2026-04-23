package fpt.project.NeoNHS.dto.request.point.historyAudio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class ElevenLabRequest {
    private String text;
    @JsonProperty("model_id")
    private String modelId;
//    @JsonProperty("language_code")
//    private String languageCode;
    @JsonProperty("apply_text_normalization")
    @Builder.Default
    private String applyTextNormalization = "auto";

    @JsonProperty("apply_language_text_normalization")
    @Builder.Default
    private boolean applyLanguageTextNormalization = false;

    @JsonProperty("voice_settings")
    private ElevenLabVoiceSettings voiceSettings;

    @Override
    public String toString() {
        return String.format("ElevenLabRequest{text='%s', modelId='%s', languageCode='%s', applyTextNormalization='%s', " +
                        "applyLanguageTextNormalization=%b, voiceSettings=%s}",
        text, modelId, "optional", applyTextNormalization, applyLanguageTextNormalization, voiceSettings);
    }
}
