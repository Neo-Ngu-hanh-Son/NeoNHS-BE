package fpt.project.NeoNHS.dto.request.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Event name is required")
    private String name;

    private String shortDescription;

    private String fullDescription;

    private String locationName;

    private String latitude;

    private String longitude;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private Boolean isTicketRequired;

    private BigDecimal price;

    private Integer maxParticipants;

    @NotBlank(message = "Thumbnail URL is required")
    private String thumbnailUrl;

    private List<UUID> tagIds;
}
