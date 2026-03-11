package fpt.project.NeoNHS.dto.response.point;

import fpt.project.NeoNHS.dto.request.point.AudioMetadataRequest;
import fpt.project.NeoNHS.dto.request.point.WordTimingRequest;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryAudioResponse {
  private UUID id;
  private UUID pointId;
  private String audioUrl;
  private String historyText;
  private List<WordTimingRequest> words;
  private AudioMetadataRequest metadata;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
