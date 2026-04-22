package fpt.project.NeoNHS.dto.request.point.historyAudio;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HistoryAudioTranslateRequest {
    @Size(min = 2, max = 100)
    private String title;
    @Size(min = 2, max = 100)
    private String author;
    @Size(min = 2, max = 2000)
    private String script;
    private String[] requiredLanguages;
}
