package fpt.project.NeoNHS.dto.request.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTagRequest {

    private String name;

    private String description;

    private String tagColor;

    private String iconUrl;
}
