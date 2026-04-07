package fpt.project.NeoNHS.dto.response.event;

import fpt.project.NeoNHS.entity.EventPointTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPointTagResponse {
    private UUID id;
    private String name;
    private String description;
    private String tagColor;
    private String iconUrl;

    public static EventPointTagResponse fromEntity(EventPointTag tag) {
        if (tag == null) return null;
        return EventPointTagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .tagColor(tag.getTagColor())
                .iconUrl(tag.getIconUrl())
                .build();
    }
}
