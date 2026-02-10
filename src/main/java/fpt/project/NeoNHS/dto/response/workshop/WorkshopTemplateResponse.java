package fpt.project.NeoNHS.dto.response.workshop;

import fpt.project.NeoNHS.enums.WorkshopStatus;
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
public class WorkshopTemplateResponse {

    private UUID id;
    private String name;
    private String shortDescription;
    private String fullDescription;
    private Integer estimatedDuration;
    private BigDecimal defaultPrice;
    private Integer minParticipants;
    private Integer maxParticipants;
    private WorkshopStatus status;
    private BigDecimal averageRating;
    private Integer totalReview;
    private UUID vendorId;
    private String vendorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Approval tracking fields
    private String rejectReason;
    private UUID approvedBy;
    private LocalDateTime approvedAt;

    private List<WorkshopImageResponse> images;
    private List<WTagResponse> tags;
}
