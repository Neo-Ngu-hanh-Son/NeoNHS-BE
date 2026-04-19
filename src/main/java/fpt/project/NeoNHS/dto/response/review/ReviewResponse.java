package fpt.project.NeoNHS.dto.response.review;

import fpt.project.NeoNHS.dto.response.user.UserResponse;
import fpt.project.NeoNHS.enums.ReviewTypeFlagEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    LocalDateTime createdAt;
    List<String> imageUrls;
}
