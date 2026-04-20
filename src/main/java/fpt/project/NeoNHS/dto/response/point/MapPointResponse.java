package fpt.project.NeoNHS.dto.response.point;

import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.entity.WorkshopTemplate;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Response DTO for points that are displayed on the map, which can represent
 * both events and workshops.
 * This class extends PointResponse to include common point fields, and adds
 * specific fields relevant to events and workshops.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MapPointResponse extends PointResponse {
        // Fields for events and workshops (Which are also points)
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer maxParticipants;
        private Integer currentEnrolled;
        private String workshopOrganizerName;

        public static MapPointResponse fromPoint(Point p) {
                return MapPointResponse.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .thumbnailUrl(p.getThumbnailUrl())
                                .latitude(p.getLatitude().doubleValue())
                                .longitude(p.getLongitude().doubleValue())
                                .estTimeSpent(p.getEstTimeSpent())
                                .type(p.getType())
                                .attractionId(p.getAttraction() != null ? p.getAttraction().getId() : null)
                                .panoramas(Collections.emptyList()) // Map does not need to know about panoramas
                                .googlePlaceId(p.getGooglePlaceId())
                                .historyAudioCount(p.getHistoryAudios() != null ? p.getHistoryAudios().size() : 0)
                                .checkinPoints(p.getCheckinPoints() != null ? p.getCheckinPoints().stream()
                                                .map(cp -> CheckinPointResponse.fromEntity(cp, null))
                                                .toList() : null)
                                .build();
        }

        public static MapPointResponse fromEventPoint(Event e) {
                String eventImage = (e.getEventImages() != null && !e.getEventImages().isEmpty())
                                ? e.getEventImages().get(0).getImageUrl()
                                : null;

                // Mapping basic point fields, then the events-specific fields
                return MapPointResponse.builder()
                                .id(e.getId())
                                .name(e.getName())
                                .description(e.getShortDescription())
                                .thumbnailUrl(eventImage)
                                .longitude(Double.valueOf(e.getLongitude()))
                                .latitude(Double.valueOf(e.getLatitude()))
                                .type(fpt.project.NeoNHS.enums.PointType.EVENT)
                                // Event specific fields
                                .startTime(e.getStartTime())
                                .endTime(e.getEndTime())
                                .maxParticipants(e.getMaxParticipants())
                                .currentEnrolled(e.getCurrentEnrolled())
                                .build();
        }

        public static MapPointResponse fromWorkshopTemplate(WorkshopTemplate wt) {
                String latitude = wt.getVendor().getLatitude();
                String longitude = wt.getVendor().getLongitude();
                String organizerName = wt.getVendor().getBusinessName();
                String wsImage = wt.getWorkshopImages().getFirst().getImageUrl();

                // Workshop sessions should already sort by time in the query
                WorkshopSession nextUpcomingWorkshop = wt.getWorkshopSessions().getFirst();

                return MapPointResponse.builder()
                                .id(wt.getId())
                                .name(wt.getName())
                                .description(wt.getShortDescription())
                                .thumbnailUrl(wsImage)
                                .latitude(Double.valueOf(latitude))
                                .longitude(Double.valueOf(longitude))
                                .type(fpt.project.NeoNHS.enums.PointType.WORKSHOP)
                                // WS specific fields
                                .startTime(nextUpcomingWorkshop.getStartTime())
                                .endTime(nextUpcomingWorkshop.getStartTime())
                                .maxParticipants(nextUpcomingWorkshop.getMaxParticipants())
                                .currentEnrolled(nextUpcomingWorkshop.getCurrentEnrolled())
                                .workshopOrganizerName(organizerName)
                                .build();
        }
}
