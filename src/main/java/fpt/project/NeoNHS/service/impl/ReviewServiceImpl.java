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

    @Override
    @Transactional
    public ReviewResponse createReview(UUID userId, CreateReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // specific the workshop template
        WorkshopTemplate workshopTemplate = workshopTemplateRepository.findById(request.getWorkshopTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", request.getWorkshopTemplateId()));

        // Check if user already reviewed this workshop
        if (reviewRepository.existsByUser_IdAndWorkshopTemplate_IdAndDeletedAtIsNull(userId, request.getWorkshopTemplateId())) {
            throw new BadRequestException("You have already reviewed this workshop. Please update your existing review instead.");
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(user)
                .workshopTemplate(workshopTemplate)
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
        updateWorkshopStats(workshopTemplate);

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
            // Clear existing images (orphanRemoval should handle deletion if setup, but simpler to recreate list or update manually)
            // Assuming Review has cascade ALL for reviewImages
            if (review.getReviewImages() != null) {
                review.getReviewImages().clear();
            } else {
                review.setReviewImages(new ArrayList<>());
            }

            if (!request.getImageUrls().isEmpty()) {
                List<ReviewImage> images = request.getImageUrls().stream()
                        .map(url -> ReviewImage.builder()
                                .imageUrl(url) // Assuming imageUrl field
                                .review(review)
                                .build())
                        .collect(Collectors.toList());
                review.getReviewImages().addAll(images);
            }
        }

        Review savedReview = reviewRepository.save(review);

        // Update stats
        updateWorkshopStats(review.getWorkshopTemplate());

        return mapToResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviewsByWorkshopTemplateId(UUID workshopTemplateId, Pageable pageable) {
        if (!workshopTemplateRepository.existsById(workshopTemplateId)) {
            throw new ResourceNotFoundException("WorkshopTemplate", "id", workshopTemplateId);
        }

        Page<Review> reviewPage = reviewRepository.findByWorkshopTemplateIdAndStatus(workshopTemplateId, ReviewStatus.VISIBLE, pageable);

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

    private void updateWorkshopStats(WorkshopTemplate workshopTemplate) {
        Double avgRating = reviewRepository.getAverageRatingByWorkshopTemplateId(workshopTemplate.getId(), ReviewStatus.VISIBLE);
        Long totalReviews = reviewRepository.countByWorkshopTemplateIdAndStatus(workshopTemplate.getId(), ReviewStatus.VISIBLE);

        workshopTemplate.setAverageRating(BigDecimal.valueOf(avgRating != null ? avgRating : 0.0));
        workshopTemplate.setTotalRatings(totalReviews.intValue());

        workshopTemplateRepository.save(workshopTemplate);
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
                .workshopTemplateId(review.getWorkshopTemplate() != null ? review.getWorkshopTemplate().getId() : null)
                .user(userResponse)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}
