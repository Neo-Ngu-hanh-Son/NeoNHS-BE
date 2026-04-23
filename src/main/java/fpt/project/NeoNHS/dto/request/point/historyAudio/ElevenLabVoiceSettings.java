package fpt.project.NeoNHS.dto.request.point.historyAudio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class ElevenLabVoiceSettings {
    private double stability;
    @JsonProperty("use_speaker_boost")
    private boolean useSpeakerBoost;
    @JsonProperty("similarity_boost")
    private double similarityBoost;
    @JsonProperty("speed")
    private double speed;

    @Override
    public String toString() {
        return String.format("\nElevenLabVoiceSettings{stability=%.2f, useSpeakerBoost=%b, similarityBoost=%.2f, speed=%.2f}",
                stability, useSpeakerBoost, similarityBoost, speed);
    }
}
