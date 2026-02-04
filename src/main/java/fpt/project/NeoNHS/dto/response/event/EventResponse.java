package fpt.project.NeoNHS.dto.response.event;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<TagResponse> tags;

    public static EventResponse fromEntity(Event event) {
        List<TagResponse> tagResponses = event.getEventTags() != null
                ? event.getEventTags().stream()
                    .map(eventTag -> TagResponse.fromEntity(eventTag.getETag()))
                    .toList()
                : Collections.emptyList();

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
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .tags(tagResponses)
                .build();
    }
}
