package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.blog.BlogCategoryResponse;
import fpt.project.NeoNHS.enums.BlogCategoryStatus;
import fpt.project.NeoNHS.service.BlogCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/blog-categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Blog Categories", description = "Admin APIs for managing blog categories (requires ADMIN role)")
public class AdminBlogCategoryController {

  private final BlogCategoryService blogCategoryService;

  @Operation(summary = "Get all blog categories", description = "Retrieve a paginated list of blog categories with optional search and status filter")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<BlogCategoryResponse>>> getBlogCategories(
      @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) BlogCategoryStatus status,
      @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
      @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

    Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<BlogCategoryResponse> categories = blogCategoryService.getBlogCategories(search, status, pageable);
    return ResponseEntity.ok(ApiResponse.success("Blog categories retrieved successfully", categories));
  }
}
