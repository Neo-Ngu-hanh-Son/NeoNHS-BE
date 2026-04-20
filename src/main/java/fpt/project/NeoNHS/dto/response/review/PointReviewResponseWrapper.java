package fpt.project.NeoNHS.dto.response.review;

import fpt.project.NeoNHS.dto.response.PagedResponse;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Pageable;

@Data
@Builder
public class PointReviewResponseWrapper {
    private PagedResponse<PointReviewResponse> reviews;
    private Long totalReviews;
    private double avgRating;
}
