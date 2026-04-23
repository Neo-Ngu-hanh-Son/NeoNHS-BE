package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.blog.BlogCategoryResponse;
import fpt.project.NeoNHS.enums.BlogCategoryStatus;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.service.BlogCategoryService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/blogs/categories")
@AllArgsConstructor
public class BlogCategoryController {
    private final BlogCategoryService categoryService;

    @GetMapping()
    public ApiResponse<Page<BlogCategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        var pageable = PageRequest.of(page, size, sort);
        var res = categoryService.getBlogCategories(name, BlogCategoryStatus.ACTIVE, pageable);
        return ApiResponse.<Page<BlogCategoryResponse>>builder()
                .data(res)
                .message("Get categories successfully")
                .success(true)
                .build();
    }
}
