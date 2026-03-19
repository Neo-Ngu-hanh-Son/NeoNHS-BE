package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.review.CreateReviewRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.dto.response.review.ReviewResponse;
import fpt.project.NeoNHS.entity.User;

import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review APIs")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('TOURIST')")
    @Operation(summary = "Create a review for a workshop template", description = "Only TOURIST can create reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            Principal principal,
            @RequestBody @Valid CreateReviewRequest request) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getName()));

        ReviewResponse response = reviewService.createReview(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Review created successfully", response));
    }

    @GetMapping("/workshops/{workshopTemplateId}")
    @Operation(summary = "Get reviews by workshop template ID")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getReviewsByWorkshopTemplateId(
            @PathVariable UUID workshopTemplateId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PagedResponse<ReviewResponse> response = reviewService.getReviewsByWorkshopTemplateId(workshopTemplateId, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Reviews retrieved successfully", response));
    }
}
