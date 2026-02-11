package fpt.project.NeoNHS.dto.request.attraction;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
public class AttractionFilterRequest {
    private String name;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openHour;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeHour;
}
