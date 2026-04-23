package fpt.project.NeoNHS.dto.request.point;

import fpt.project.NeoNHS.dto.request.point.historyAudio.AudioMetadataRequest;
import fpt.project.NeoNHS.dto.request.point.historyAudio.WordTimingRequest;
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
