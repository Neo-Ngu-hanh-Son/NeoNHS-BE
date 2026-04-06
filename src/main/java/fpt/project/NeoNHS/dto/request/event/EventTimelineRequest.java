package fpt.project.NeoNHS.dto.request.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTimelineRequest {

    @NotBlank(message = "Timeline name is required")
    private String name;

    private String description;

    private String organizer;

    private String coOrganizer;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String lunarDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private UUID eventPointId;
}
