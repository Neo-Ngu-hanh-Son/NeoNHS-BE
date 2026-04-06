package fpt.project.NeoNHS.dto.response.vendor.dashboard;

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
public class VendorSessionItem {
    private UUID workshopId;
    private String workshopName;
    private UUID sessionId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private int remainingSlots;
}
