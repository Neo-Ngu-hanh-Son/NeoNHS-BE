package fpt.project.NeoNHS.dto.response.workshop;

import fpt.project.NeoNHS.enums.SessionStatus;
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
public class WorkshopSessionResponse {

    // Session-specific fields
    private UUID id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private Integer maxParticipants;
    private Integer currentEnrolled;
    private Integer availableSlots;
    private SessionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Template information (inherited for user display)
    private UUID workshopTemplateId;
    private String name;
    private String shortDescription;
    private String fullDescription;
    private Integer estimatedDuration;
    private BigDecimal averageRating;
    private Integer totalReview;

    // Vendor information
    private UUID vendorId;
    private String vendorName;

    // Related entities
    private List<WorkshopImageResponse> images;
    private List<WTagResponse> tags;
}
