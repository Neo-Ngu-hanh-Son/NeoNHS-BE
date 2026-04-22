package fpt.project.NeoNHS.dto.response.point.historyAudio;
public record WordAlignment(
        String text,
        Double start,
        Double end,
        Double loss
) {}
