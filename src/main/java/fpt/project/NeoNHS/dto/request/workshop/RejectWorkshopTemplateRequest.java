package fpt.project.NeoNHS.dto.request.workshop;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectWorkshopTemplateRequest {

    @NotBlank(message = "Reject reason is required")
    private String rejectReason;
}
