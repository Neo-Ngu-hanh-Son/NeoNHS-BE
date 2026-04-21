package fpt.project.NeoNHS.dto.response.point.historyAudio;
import java.util.List;

public record ForcedAlignmentResponse(
        List<CharacterAlignment> characters,
        List<WordAlignment> words,
        Double loss
) {}

