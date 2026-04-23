package fpt.project.NeoNHS.dto.request.point.historyAudio;

import lombok.Data;

@Data
public class WordTimingRequest {
    private String text;
    private Double start;
    private Double end;
}
