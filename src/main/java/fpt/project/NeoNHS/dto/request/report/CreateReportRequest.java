package fpt.project.NeoNHS.dto.request.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

import fpt.project.NeoNHS.enums.ReportType;

@Data
public class CreateReportRequest {
    @NotNull(message = "Target ID is required")
    private UUID targetId;

    @NotBlank(message = "Target type is required")
    private ReportType targetType;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String description;

    private String evidenceUrl;
}