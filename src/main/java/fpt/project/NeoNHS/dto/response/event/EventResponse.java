package fpt.project.NeoNHS.dto.response.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventResponse {

    private UUID id;

    private String name;

    private String shortDescription;

    private String fullDescription;

    private String locationName;

    private String latitude;

    private String longitude;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Boolean isTicketRequired;

    private BigDecimal price;

    private Integer maxParticipants;

    private Integer currentEnrolled;

    private EventStatus status;

    private String thumbnailUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private List<TagResponse> tags;

    /**
     * Only populated in detail view (getEventById).
     */
    private List<EventImageResponse> images;

    /**
     * For list view: includes all event info + thumbnail URL, no image album.
     */
    public static EventResponse fromEntity(Event event) {
        List<TagResponse> tagResponses = event.getEventTags() != null
                ? event.getEventTags().stream()
                    .map(eventTag -> TagResponse.fromEntity(eventTag.getETag()))
                    .toList()
                : Collections.emptyList();

        // Extract thumbnail URL from eventImages
        String thumbnail = null;
        if (event.getEventImages() != null) {
            thumbnail = event.getEventImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                    .map(img -> img.getImageUrl())
                    .findFirst()
                    .orElse(null);
        }

        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .shortDescription(event.getShortDescription())
                .fullDescription(event.getFullDescription())
                .locationName(event.getLocationName())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .isTicketRequired(event.getIsTicketRequired())
                .price(event.getPrice())
                .maxParticipants(event.getMaxParticipants())
                .currentEnrolled(event.getCurrentEnrolled())
                .status(event.getStatus())
                .thumbnailUrl(thumbnail)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .deletedAt(event.getDeletedAt())
                .tags(tagResponses)
                .build();
    }

    /**
     * For detail view: includes all event info + thumbnail URL + full image album.
     */
    public static EventResponse fromEntityWithImages(Event event) {
        EventResponse response = fromEntity(event);

        // Populate all images (non-deleted)
        if (event.getEventImages() != null) {
            List<EventImageResponse> imageResponses = event.getEventImages().stream()
                    .filter(img -> img.getDeletedAt() == null)
                    .map(EventImageResponse::fromEntity)
                    .toList();
            response.setImages(imageResponses);
        } else {
            response.setImages(Collections.emptyList());
        }

        return response;
    }
}
