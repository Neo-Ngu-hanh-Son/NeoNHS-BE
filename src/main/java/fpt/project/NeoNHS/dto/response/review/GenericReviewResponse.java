package fpt.project.NeoNHS.dto.response.review;

import fpt.project.NeoNHS.dto.response.user.UserResponse;
import fpt.project.NeoNHS.entity.Review;
import fpt.project.NeoNHS.enums.ReviewTypeFlagEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class GenericReviewResponse {
    UUID id;
    UUID reviewTypeId;
    ReviewTypeFlagEnum reviewTypeFlg;
    UserResponse user;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
    List<ReviewImageResponse> reviewImages;

    public static GenericReviewResponse fromEntity(Review review) {
        return GenericReviewResponse.builder()
                .id(review.getId())
                .reviewTypeId(review.getReviewTypeId())
                .reviewTypeFlg(review.getReviewTypeFlg())
                .user(UserResponse.fromEntity(review.getUser()))
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .reviewImages(review.getReviewImages().stream().map(ReviewImageResponse::fromEntity).toList())
                .build();
    }
}
