package fpt.project.NeoNHS.dto.response.event;

import fpt.project.NeoNHS.entity.EventImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventImageResponse {

    private UUID id;

    private String imageUrl;

    private Boolean isThumbnail;

    private LocalDateTime createdAt;

    public static EventImageResponse fromEntity(EventImage eventImage) {
        return EventImageResponse.builder()
                .id(eventImage.getId())
                .imageUrl(eventImage.getImageUrl())
                .isThumbnail(eventImage.getIsThumbnail())
                .createdAt(eventImage.getCreatedAt())
                .build();
    }
}
