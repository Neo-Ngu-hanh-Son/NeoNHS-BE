package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.review.CreateReviewRequest;
import fpt.project.NeoNHS.dto.request.review.UpdateReviewRequest;
import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.dto.response.review.GenericReviewResponseWrapper;
import fpt.project.NeoNHS.dto.response.review.ReviewResponse;
import fpt.project.NeoNHS.enums.ReviewTypeFlagEnum;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    ReviewResponse createReview(UUID userId, CreateReviewRequest request);
    ReviewResponse updateReview(UUID userId, UUID reviewId, UpdateReviewRequest request);

    PagedResponse<ReviewResponse> getReviewsForWorkshopTemplate(UUID workshopTemplateId, Pageable pageable);

    GenericReviewResponseWrapper getReviews(UUID pointId, ReviewTypeFlagEnum flag, Pageable pageable);
}
