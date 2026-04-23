package fpt.project.NeoNHS.dto.request.event;

import fpt.project.NeoNHS.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFilterRequest {

    private EventStatus status;

    private Boolean isTicketRequired;

    private String name;

    private String location;

    private LocalDate startDate;

    private LocalDate endDate;


    private List<UUID> tagIds;

    /**
     * Filter by soft delete status (Admin only):
     * - null or false: show only active (non-deleted) events
     * - true: show only deleted events
     */
    private Boolean deleted;

    /**
     * Include all events regardless of deleted status (Admin only).
     * If true, ignores the 'deleted' field and shows all events.
     */
    @Builder.Default
    private Boolean includeDeleted = false;
}
