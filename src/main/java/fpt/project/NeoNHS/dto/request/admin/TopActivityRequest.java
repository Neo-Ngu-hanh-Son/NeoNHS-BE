package fpt.project.NeoNHS.dto.request.admin;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopActivityRequest {

    /**
     * WORKSHOP | EVENT
     */
    private String type;

    /**
     * Top N
     */
    private Integer limit;
}
