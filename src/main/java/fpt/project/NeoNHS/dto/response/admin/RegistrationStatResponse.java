package fpt.project.NeoNHS.dto.response.admin;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationStatResponse {
    private String period;
    private long count;
}