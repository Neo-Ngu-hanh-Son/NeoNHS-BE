package fpt.project.NeoNHS.dto.response.review;

import fpt.project.NeoNHS.dto.response.user.UserResponse;
import fpt.project.NeoNHS.entity.Review;
import fpt.project.NeoNHS.enums.ReviewTypeFlagEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    UUID id;
    UUID reviewTypeId;
    ReviewTypeFlagEnum reviewTypeFlg;
    UserResponse user;
    Integer rating;
    String comment;
    List<String> imageUrls;
    LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .reviewTypeId(r.getReviewTypeId())
                .reviewTypeFlg(r.getReviewTypeFlg())
                .user(r.getUser() != null ? UserResponse.fromEntity(r.getUser()) : null)
                .rating(r.getRating())
                .comment(r.getComment())
                .imageUrls(r.getReviewImages() != null ? r.getReviewImages().stream().map(fpt.project.NeoNHS.entity.ReviewImage::getImageUrl).collect(toList()) : new java.util.ArrayList<>())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
