package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.blog.BlogRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.blog.BlogResponse;
import fpt.project.NeoNHS.enums.BlogStatus;
import fpt.project.NeoNHS.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/blogs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Blogs", description = "Admin APIs for managing blogs (requires ADMIN role)")
public class AdminBlogController {

  private final BlogService blogService;

  @Operation(summary = "Get all blogs", description = "Retrieve a paginated list of blogs with optional search and status filter")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<BlogResponse>>> getBlogs(
      @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) BlogStatus status,
      @RequestParam(required = false) List<String> tags,
      @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
      @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

    Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<BlogResponse> blogs = blogService.getBlogs(search, status, tags, pageable);
    return ResponseEntity.ok(ApiResponse.success("Blogs retrieved successfully", blogs));
  }

  @Operation(summary = "Get blog by ID", description = "Retrieve a single blog by its ID")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<BlogResponse>> getBlogById(
      @Parameter(description = "Blog ID") @PathVariable UUID id) {
    BlogResponse blog = blogService.getBlogById(id);
    return ResponseEntity.ok(ApiResponse.success("Blog retrieved successfully", blog));
  }

  @Operation(summary = "Create blog", description = "Create a new blog")
  @PostMapping
  public ResponseEntity<ApiResponse<BlogResponse>> createBlog(
      @Valid @RequestBody BlogRequest request) {
    BlogResponse blog = blogService.createBlog(request);
    return ResponseEntity.ok(ApiResponse.success("Blog created successfully", blog));
  }

  @Operation(summary = "Update blog", description = "Update an existing blog")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<BlogResponse>> updateBlog(
      @Parameter(description = "Blog ID") @PathVariable UUID id,
      @Valid @RequestBody BlogRequest request) {
    BlogResponse blog = blogService.updateBlog(id, request);
    return ResponseEntity.ok(ApiResponse.success("Blog updated successfully", blog));
  }

  @Operation(summary = "Delete blog", description = "Soft delete a blog")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteBlog(
      @Parameter(description = "Blog ID") @PathVariable UUID id) {
    blogService.deleteBlog(id);
    return ResponseEntity.ok(ApiResponse.success("Blog deleted successfully", null));
  }
}
