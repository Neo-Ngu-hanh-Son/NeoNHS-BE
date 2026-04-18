package fpt.project.NeoNHS.dto.request.review;

import fpt.project.NeoNHS.enums.ReviewTypeFlagEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

    @NotNull(message = "Review type ID is required")
    UUID reviewTypeId;

    /**
     *     WORKSHOP,
     *     EVENT,
     *     POINT
     */
    @NotNull(message = "Review type flag is required")
    ReviewTypeFlagEnum reviewTypeFlg;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    Integer rating;

    String comment;

    List<String> imageUrls;
}
