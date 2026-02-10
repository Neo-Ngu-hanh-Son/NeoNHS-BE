package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.blog.BlogCategoryResponse;
import fpt.project.NeoNHS.entity.BlogCategory;
import fpt.project.NeoNHS.enums.BlogCategoryStatus;
import fpt.project.NeoNHS.repository.BlogCategoryRepository;
import fpt.project.NeoNHS.service.BlogCategoryService;
import fpt.project.NeoNHS.specification.BlogCategorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlogCategoryServiceImpl implements BlogCategoryService {

    private final BlogCategoryRepository blogCategoryRepository;

    @Override
    public Page<BlogCategoryResponse> getBlogCategories(String search, BlogCategoryStatus status, Pageable pageable) {
        Specification<BlogCategory> spec = BlogCategorySpecification.withFilters(search, status);
        return blogCategoryRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    private BlogCategoryResponse mapToResponse(BlogCategory category) {
        return BlogCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .status(category.getStatus())
                .postCount(category.getBlogs() != null ? category.getBlogs().size() : 0)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
