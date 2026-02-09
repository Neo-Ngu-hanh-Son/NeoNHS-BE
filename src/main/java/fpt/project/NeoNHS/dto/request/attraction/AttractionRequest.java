package fpt.project.NeoNHS.dto.request.attraction;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class AttractionRequest {
    private String name;
    private String description;
    private String mapImageUrl;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private String thumbnailUrl;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime openHour;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeHour;
}
