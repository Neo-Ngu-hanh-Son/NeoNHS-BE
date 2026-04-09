package fpt.project.NeoNHS.dto.request.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPointTagRequest {
    private String id;
    @NotBlank(message = "Tag name is required")
    @Size(max = 255, message = "Tag name must not exceed 255 characters")
    private String name;

    private String description;

    private String tagColor;

    private String iconUrl;
}
