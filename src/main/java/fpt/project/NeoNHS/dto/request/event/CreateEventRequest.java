package fpt.project.NeoNHS.dto.request.event;

import jakarta.validation.constraints.*;
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
    @Size(max = 255, message = "Event name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Short description must not exceed 255 characters")
    private String shortDescription;

    private String fullDescription;

    @Size(max = 255, message = "Location name must not exceed 255 characters")
    private String locationName;

    private String latitude;

    private String longitude;

    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time must be in the present or future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    private Boolean isTicketRequired;

    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;

    @Positive(message = "Max participants must be positive")
    private Integer maxParticipants;

    @NotBlank(message = "Thumbnail URL is required")
    @Size(max = 255, message = "Thumbnail URL must not exceed 255 characters")
    private String thumbnailUrl;

    private List<UUID> tagIds;
}
