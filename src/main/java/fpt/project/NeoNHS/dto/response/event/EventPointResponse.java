package fpt.project.NeoNHS.dto.response.event;

import fpt.project.NeoNHS.entity.EventPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPointResponse {
    private UUID id;
    private String name;
    private String description;
    private String imageList;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private EventPointTagResponse eventPointTag;

    public static EventPointResponse fromEntity(EventPoint point) {
        if (point == null) return null;
        return EventPointResponse.builder()
                .id(point.getId())
                .name(point.getName())
                .description(point.getDescription())
                .imageList(point.getImageList())
                .latitude(point.getLatitude())
                .longitude(point.getLongitude())
                .address(point.getAddress())
                .eventPointTag(EventPointTagResponse.fromEntity(point.getEventPointTag()))
                .build();
    }
}
