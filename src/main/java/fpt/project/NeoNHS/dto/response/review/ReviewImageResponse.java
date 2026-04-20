package fpt.project.NeoNHS.dto.response.review;

import fpt.project.NeoNHS.entity.ReviewImage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewImageResponse {
    private String imageUrl;
    private String authorName;
    private String authorId;
    private LocalDateTime takenDate;

    public static ReviewImageResponse fromEntity(ReviewImage reviewImage) {
        return ReviewImageResponse.builder()
                .imageUrl(reviewImage.getImageUrl())
                .authorName(reviewImage.getReview().getUser().getFullname())
                .authorId(reviewImage.getReview().getUser().getId().toString())
                .takenDate(reviewImage.getCreatedAt())
                .build();
    }
}
