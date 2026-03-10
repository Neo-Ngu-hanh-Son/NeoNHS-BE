package fpt.project.NeoNHS.dto.request.admin;

import fpt.project.NeoNHS.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveReportRequest {
    @NotNull(message = "Status is required")
    private ReportStatus status;

    @NotBlank(message = "Note is required for resolution")
    private String handleNote;
}
