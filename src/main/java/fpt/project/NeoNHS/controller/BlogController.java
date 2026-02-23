package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.blog.BlogResponse;
import fpt.project.NeoNHS.enums.BlogStatus;
import fpt.project.NeoNHS.service.BlogCategoryService;
import fpt.project.NeoNHS.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;
    private final BlogCategoryService blogCategoryService;

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

    @GetMapping("/slug/{slug}")
    public ApiResponse<BlogResponse> getBlogBySlug(@PathVariable String slug) {
        var blog = blogService.getBlogBySlug(slug);
        return ApiResponse.success("Blog retrieved successfully", blog);
    }

    @GetMapping("/{id}")
    public ApiResponse<BlogResponse> getBlogById(@PathVariable UUID id) {
        var blog = blogService.getBlogById(id);
        return ApiResponse.success("Blog retrieved successfully", blog);
    }
}
