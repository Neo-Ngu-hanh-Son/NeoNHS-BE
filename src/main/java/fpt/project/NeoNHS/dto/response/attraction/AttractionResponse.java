package fpt.project.NeoNHS.dto.response.attraction;

import fpt.project.NeoNHS.enums.AttractionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class AttractionResponse {
    private UUID id;
    private String name;
    private String description;
    private String mapImageUrl;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private AttractionStatus status;
    private String thumbnailUrl;
    private LocalTime openHour;
    private LocalTime closeHour;
}
