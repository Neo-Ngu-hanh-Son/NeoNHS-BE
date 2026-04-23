package fpt.project.NeoNHS.dto.response.review;

import fpt.project.NeoNHS.dto.response.PagedResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericReviewResponseWrapper {
    private PagedResponse<GenericReviewResponse> reviews;
    private Long totalReviews;
    private double avgRating;
}
