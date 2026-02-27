package fpt.project.NeoNHS.dto.response.admin;

import lombok.*;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopActivityResponse {
    private UUID id;
    private String name;
    private long ticketsSold;
}
