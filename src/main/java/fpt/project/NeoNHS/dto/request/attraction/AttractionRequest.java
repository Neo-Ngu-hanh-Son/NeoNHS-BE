package fpt.project.NeoNHS.dto.request.attraction;

import com.fasterxml.jackson.annotation.JsonFormat;
import fpt.project.NeoNHS.enums.AttractionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionRequest {
    private String name;
    private String description;
    private String mapImageUrl;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private AttractionStatus status;
    private String thumbnailUrl;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime openHour;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeHour;
}
