package fpt.project.NeoNHS.dto.request.workshop;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkshopTemplateRequest {

    @NotBlank(message = "Workshop name is required")
    @Size(max = 255, message = "Workshop name must not exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    private String fullDescription;

    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDuration;

    @DecimalMin(value = "0.0", inclusive = false, message = "Default price must be greater than 0")
    private BigDecimal defaultPrice;

    @Positive(message = "Minimum participants must be positive")
    private Integer minParticipants;

    @Positive(message = "Maximum participants must be positive")
    private Integer maxParticipants;

    private List<String> imageUrls;

    @Min(value = 0, message = "Thumbnail index must be non-negative")
    private Integer thumbnailIndex;

    @NotNull(message = "At least one tag is required")
    @Size(min = 1, message = "At least one tag is required")
    private List<UUID> tagIds;
}
