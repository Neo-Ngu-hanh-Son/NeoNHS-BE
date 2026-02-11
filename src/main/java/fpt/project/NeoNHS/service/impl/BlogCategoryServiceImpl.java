package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.blog.BlogCategoryRequest;
import fpt.project.NeoNHS.dto.response.blog.BlogCategoryResponse;
import fpt.project.NeoNHS.entity.BlogCategory;
import fpt.project.NeoNHS.enums.BlogCategoryStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.helpers.SlugGenerator;
import fpt.project.NeoNHS.repository.BlogCategoryRepository;
import fpt.project.NeoNHS.repository.BlogRepository;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.BlogCategoryService;
import fpt.project.NeoNHS.specification.BlogCategorySpecification;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlogCategoryServiceImpl implements BlogCategoryService {

    private final BlogCategoryRepository blogCategoryRepository;
    private final BlogRepository blogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BlogCategoryResponse> getBlogCategories(String search, BlogCategoryStatus status, Pageable pageable) {
        Specification<BlogCategory> spec = BlogCategorySpecification.withFilters(search, status);
        return blogCategoryRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteBlogCategory(UUID categoryId) {
        UserPrincipal admin = getCurrentUserPrincipal();
        BlogCategory category = blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found"));

        if (category.getDeletedAt() != null) {
            throw new BadRequestException("Blog category is already deleted");
        }

        long postCount = blogRepository.countByBlogCategoryId(categoryId);
        if (postCount > 0) {
            throw new BadRequestException("Cannot delete category. It is being used by existing blog posts.");
        }

        category.setDeletedAt(LocalDateTime.now());
        category.setDeletedBy(admin.getId());
        category.setUpdatedBy(admin.getId());
        category.setStatus(BlogCategoryStatus.ARCHIVED);
        blogCategoryRepository.save(category);
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

    @Override
    public void createBlogCategory(BlogCategoryRequest request) {
        UserPrincipal admin = getCurrentUserPrincipal();
        if (blogCategoryRepository.existsByNameContaining(request.getName())) {
            throw new BadRequestException("Category name already exists");
        }
        String slug = SlugGenerator.generateSlug(request.getName());
        if (blogCategoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category slug already exists");
        }
        BlogCategory category = BlogCategory.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .status(request.getStatus())
                .updatedBy(admin.getId())
                .build();
        blogCategoryRepository.save(category);
    }

    @Override
    public void updateBlogCategory(UUID categoryId, BlogCategoryRequest request) {
        UserPrincipal admin = getCurrentUserPrincipal();
        BlogCategory category = blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found"));
        // if category is deleted, only allow to update status to active, otherwise
        // throw exception
        if (category.getDeletedAt() != null) {
            throw new BadRequestException("Blog category is already deleted, cannot update");
        }

        String slug = SlugGenerator.generateSlug(request.getName());
        if (blogCategoryRepository.existsBySlugAndIdNot(slug, categoryId)) {
            throw new BadRequestException("Category slug already exists");
        }
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus());
        category.setUpdatedBy(admin.getId());
        blogCategoryRepository.save(category);
    }

    @Override
    public BlogCategoryResponse getBlogCategoryById(UUID id) {
        BlogCategory category = blogCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found"));
        return mapToResponse(category);
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        return userPrincipal;
    }
}
