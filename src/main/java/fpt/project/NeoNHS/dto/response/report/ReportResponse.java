package fpt.project.NeoNHS.dto.response.report;

import fpt.project.NeoNHS.entity.Report;
import fpt.project.NeoNHS.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReportResponse {
    private UUID id;
    private UUID targetId;
    private String targetType;
    private String targetName;
    private String reason;
    private String description;
    private String evidenceUrl;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private String reporterName;
    private String handlerId;
    private String handlerName;
    private String handleNote;
    private LocalDateTime resolvedAt;

    public static ReportResponse fromEntity(Report report) {
        if (report == null) return null;

        return ReportResponse.builder()
                .id(report.getId())
                .targetId(report.getTargetId())
                .targetType(report.getTargetType().toString())
                .reason(report.getReason())
                .description(report.getDescription())
                .evidenceUrl(report.getEvidenceUrl())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .reporterName(report.getReporter() != null ? report.getReporter().getFullname() : "Unknown")
                .handlerId(report.getHandlerId())
                .handleNote(report.getHandleNote())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}