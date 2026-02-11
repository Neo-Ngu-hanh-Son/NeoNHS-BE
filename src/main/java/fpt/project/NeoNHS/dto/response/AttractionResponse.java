package fpt.project.NeoNHS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO for Attraction response in public API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttractionResponse {

    private UUID id;

    private String name;

    private String description;

    private String mapImageUrl;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String status;

    private String thumbnailUrl;

    private LocalTime openHour;

    private LocalTime closeHour;

    private Integer pointCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
