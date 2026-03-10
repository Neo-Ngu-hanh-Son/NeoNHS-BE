package fpt.project.NeoNHS.dto.response.workshop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopImageResponse {

    private UUID id;
    private String imageUrl;
    private Boolean isThumbnail;
}
