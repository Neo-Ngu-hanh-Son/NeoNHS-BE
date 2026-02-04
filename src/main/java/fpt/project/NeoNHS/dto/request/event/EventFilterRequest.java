package fpt.project.NeoNHS.dto.request.event;

import fpt.project.NeoNHS.enums.EventStatus;
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
public class EventFilterRequest {

    private EventStatus status;

    private String name;

    private String location;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private List<UUID> tagIds;
}
