package fpt.project.NeoNHS.dto.response.event;

import fpt.project.NeoNHS.entity.ETag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    private UUID id;

    private String name;

    private String description;

    private String tagColor;

    private String iconUrl;

    public static TagResponse fromEntity(ETag eTag) {
        return TagResponse.builder()
                .id(eTag.getId())
                .name(eTag.getName())
                .description(eTag.getDescription())
                .tagColor(eTag.getTagColor())
                .iconUrl(eTag.getIconUrl())
                .build();
    }
}
