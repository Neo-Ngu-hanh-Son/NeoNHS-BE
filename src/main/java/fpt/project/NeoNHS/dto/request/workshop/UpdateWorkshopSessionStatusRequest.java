package fpt.project.NeoNHS.dto.request.workshop;

import fpt.project.NeoNHS.enums.SessionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkshopSessionStatusRequest {

    @NotNull(message = "Status cannot be null")
    private SessionStatus status;

}
