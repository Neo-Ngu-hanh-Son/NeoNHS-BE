package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.review.CreateReviewRequest;
import fpt.project.NeoNHS.dto.request.review.UpdateReviewRequest;
import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.dto.response.review.ReviewResponse;
import fpt.project.NeoNHS.dto.response.user.UserResponse;
import fpt.project.NeoNHS.entity.Review;
import fpt.project.NeoNHS.entity.ReviewImage;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.WorkshopTemplate;
import fpt.project.NeoNHS.enums.ReviewStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.repository.ReviewRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final EventRepository eventRepository;
    private final PointRepository pointRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(UUID userId, CreateReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate the target exists
        validateReviewTarget(request.getReviewTypeId(), request.getReviewTypeFlg());

        // Check if user already reviewed this target
        if (reviewRepository.existsByUser_IdAndReviewTypeIdAndReviewTypeFlgAndDeletedAtIsNull(userId, request.getReviewTypeId(), request.getReviewTypeFlg())) {
            throw new BadRequestException("You have already reviewed this item. Please update your existing review instead.");
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(user)
                .reviewTypeFlg(request.getReviewTypeFlg())
                .reviewTypeId(request.getReviewTypeId())
                .status(ReviewStatus.VISIBLE)
                .build();

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ReviewImage> images = request.getImageUrls().stream()
                    .map(url -> ReviewImage.builder()
                            .imageUrl(url)
                            .review(review)
                            .build())
                    .collect(Collectors.toList());
            review.setReviewImages(images);
        }

        Review savedReview = reviewRepository.save(review);
        updateStatsIfNeeded(request.getReviewTypeId(), request.getReviewTypeFlg());

        return mapToResponse(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(UUID userId, UUID reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Check ownership
        if (!review.getUser().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to update this review");
        }

        // Update fields if present
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        // Update images if provided (replace existing)
        if (request.getImageUrls() != null) {
            if (review.getReviewImages() != null) {
                review.getReviewImages().clear();
            } else {
                review.setReviewImages(new ArrayList<>());
            }

            if (!request.getImageUrls().isEmpty()) {
                List<ReviewImage> images = request.getImageUrls().stream()
                        .map(url -> ReviewImage.builder()
                                .imageUrl(url)
                                .review(review)
                                .build())
                        .collect(Collectors.toList());
                review.getReviewImages().addAll(images);
            }
        }

        Review savedReview = reviewRepository.save(review);

        // Update stats
        updateStatsIfNeeded(review.getReviewTypeId(), review.getReviewTypeFlg());

        return mapToResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviewsForWorkshopTemplate(UUID workshopTemplateId, Pageable pageable) {
        validateReviewTarget(workshopTemplateId, 1);
        Page<Review> reviewPage = reviewRepository.pageVisibleReviewsForWorkshopTemplate(
                workshopTemplateId, ReviewStatus.VISIBLE, pageable);
        return toPagedReviewResponse(reviewPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviewsForEvent(UUID eventId, Pageable pageable) {
        validateReviewTarget(eventId, 2);
        Page<Review> reviewPage = reviewRepository.pageVisibleReviewsForEvent(eventId, ReviewStatus.VISIBLE, pageable);
        return toPagedReviewResponse(reviewPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviewsForPoint(UUID pointId, Pageable pageable) {
        validateReviewTarget(pointId, 3);
        Page<Review> reviewPage = reviewRepository.pageVisibleReviewsForPoint(pointId, ReviewStatus.VISIBLE, pageable);
        return toPagedReviewResponse(reviewPage);
    }

    private PagedResponse<ReviewResponse> toPagedReviewResponse(Page<Review> reviewPage) {
        List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ReviewResponse>builder()
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .content(reviewResponses)
                .build();
    }

    private void validateReviewTarget(UUID reviewTypeId, Integer reviewTypeFlg) {
        if (reviewTypeFlg == null) {
            throw new BadRequestException("Review type flag must not be null");
        }
        if (reviewTypeId == null) {
            throw new BadRequestException("Review type ID must not be null");
        }
        switch (reviewTypeFlg) {
            case 1: // Workshop
                if (!workshopTemplateRepository.existsById(reviewTypeId)) {
                    throw new ResourceNotFoundException("WorkshopTemplate", "id", reviewTypeId);
                }
                break;
            case 2: // Event
                if (!eventRepository.existsById(reviewTypeId)) {
                    throw new ResourceNotFoundException("Event", "id", reviewTypeId);
                }
                break;
            case 3: // Point
                if (!pointRepository.existsById(reviewTypeId)) {
                    throw new ResourceNotFoundException("Point", "id", reviewTypeId);
                }
                break;
            default:
                throw new BadRequestException("Invalid review type flag. Allowed values: 1 (Workshop), 2 (Event), 3 (Point)");
        }
    }

    private void updateStatsIfNeeded(UUID reviewTypeId, Integer reviewTypeFlg) {
        if (reviewTypeFlg != null && reviewTypeFlg == 1) { // Workshop has average rating fields
            WorkshopTemplate workshopTemplate = workshopTemplateRepository.findById(reviewTypeId).orElse(null);
            if (workshopTemplate != null) {
                Double avgRating = reviewRepository.getAverageRatingByReviewType(reviewTypeId, reviewTypeFlg, ReviewStatus.VISIBLE);
                Long totalReviews = reviewRepository.countByReviewTypeIdAndReviewTypeFlgAndStatus(reviewTypeId, reviewTypeFlg, ReviewStatus.VISIBLE);

                workshopTemplate.setAverageRating(BigDecimal.valueOf(avgRating != null ? avgRating : 0.0));
                workshopTemplate.setTotalRatings(totalReviews.intValue());

                workshopTemplateRepository.save(workshopTemplate);
            }
        }
    }

    private ReviewResponse mapToResponse(Review review) {
        List<String> imageUrls = review.getReviewImages() != null
                ? review.getReviewImages().stream().map(ReviewImage::getImageUrl).collect(Collectors.toList())
                : new ArrayList<>();

        UserResponse userResponse = UserResponse.builder()
                .id(review.getUser().getId())
                .fullname(review.getUser().getFullname())
                .email(review.getUser().getEmail())
                .avatarUrl(review.getUser().getAvatarUrl())
                .role(review.getUser().getRole())
                .build();

        return ReviewResponse.builder()
                .id(review.getId())
                .reviewTypeId(review.getReviewTypeId())
                .reviewTypeFlg(review.getReviewTypeFlg())
                .user(userResponse)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}
