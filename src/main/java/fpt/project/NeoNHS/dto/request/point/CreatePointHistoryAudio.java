package fpt.project.NeoNHS.dto.request.point;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreatePointHistoryAudio {
    private UUID pointId;
    private String audioUrl;
    private String historyText;
    private List<WordTimingRequest> words;
    private AudioMetadataRequest metadata;
}
