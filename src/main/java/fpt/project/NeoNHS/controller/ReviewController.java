package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.review.CreateReviewRequest;
import fpt.project.NeoNHS.dto.request.review.UpdateReviewRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.dto.response.review.ReviewResponse;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review APIs")
public class ReviewController {

    private static final Set<String> ALLOWED_REVIEW_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "rating");

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_TOURIST')")
    @Operation(summary = "Create a generic review (Review target change depends on the type flag)", description = "Only TOURIST can create reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            Principal principal,
            @RequestBody @Valid CreateReviewRequest request) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getName()));

        ReviewResponse response = reviewService.createReview(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Review created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TOURIST')")
    @Operation(summary = "Update an existing review", description = "Only the owner of the review can update it. Only TOURIST can review.")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            Principal principal,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateReviewRequest request) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getName()));

        ReviewResponse response = reviewService.updateReview(user.getId(), id, request);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Review updated successfully", response));
    }

    @GetMapping("/workshops/{workshopTemplateId}")
    @Operation(summary = "List reviews for a workshop template", description = "Joins reviews with workshop_templates so only reviews for that template are returned.")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getReviewsForWorkshopTemplate(
            @PathVariable UUID workshopTemplateId,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @Parameter(description = "Sort field: createdAt, updatedAt, or rating")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        PagedResponse<ReviewResponse> response = reviewService.getReviewsForWorkshopTemplate(
                workshopTemplateId, buildReviewPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Reviews retrieved successfully", response));
    }

    @GetMapping("/events/{eventId}")
    @Operation(summary = "List reviews for an event", description = "Joins reviews with events so only reviews for that event are returned.")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getReviewsForEvent(
            @PathVariable UUID eventId,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @Parameter(description = "Sort field: createdAt, updatedAt, or rating")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        PagedResponse<ReviewResponse> response = reviewService.getReviewsForEvent(
                eventId, buildReviewPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Reviews retrieved successfully", response));
    }

    @GetMapping("/points/{pointId}")
    @Operation(summary = "List reviews for a point", description = "Joins reviews with points so only reviews for that point are returned.")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getReviewsForPoint(
            @PathVariable UUID pointId,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @Parameter(description = "Sort field: createdAt, updatedAt, or rating")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        PagedResponse<ReviewResponse> response = reviewService.getReviewsForPoint(
                pointId, buildReviewPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Reviews retrieved successfully", response));
    }

    private static PageRequest buildReviewPageable(int page, int size, String sortBy, String sortDir) {
        String safeSortBy = resolveReviewSortBy(sortBy);
        int safeSize = Math.min(Math.max(size, 1), PaginationConstants.MAX_PAGE_SIZE);
        Sort sort = PaginationConstants.SORT_ASC.equalsIgnoreCase(sortDir)
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();
        return PageRequest.of(page, safeSize, sort);
    }

    private static String resolveReviewSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return PaginationConstants.DEFAULT_SORT_BY;
        }
        String trimmed = sortBy.trim();
        if (!ALLOWED_REVIEW_SORT_FIELDS.contains(trimmed)) {
            return PaginationConstants.DEFAULT_SORT_BY;
        }
        return trimmed;
    }
}
