package fpt.project.NeoNHS.dto.request.admin;

import fpt.project.NeoNHS.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportFilterRequest {
    private String targetType;
    private ReportStatus status;
    private String reporterName;
    private String targetName;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}