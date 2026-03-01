package fpt.project.NeoNHS.dto.response.admin;

import lombok.*;
import java.util.Map;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusCountResponse {
    private Map<String, Long> workshop;
    private Map<String, Long> event;
}
