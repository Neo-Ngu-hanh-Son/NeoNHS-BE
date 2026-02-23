package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.blog.BlogRequest;
import fpt.project.NeoNHS.dto.response.blog.BlogResponse;
import fpt.project.NeoNHS.entity.Blog;
import fpt.project.NeoNHS.entity.BlogCategory;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.BlogStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.helpers.SlugGenerator;
import fpt.project.NeoNHS.repository.BlogCategoryRepository;
import fpt.project.NeoNHS.repository.BlogRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.BlogService;
import fpt.project.NeoNHS.specification.BlogSpecification;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final BlogCategoryRepository blogCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> getBlogs(String search, BlogStatus status, List<String> tags, Pageable pageable) {
        var spec = BlogSpecification.withFilters(search, status, tags);
        return blogRepository.findAll(spec, pageable).map(BlogResponse::fromEntity);
    }

    @Override
    @Transactional
    public BlogResponse createBlog(BlogRequest request) {
        validateUniqueTitleForCreate(request.getTitle());
        var author = getCurrentUser();
        var blogCategory = getActiveBlogCategory(request.getBlogCategoryId());

        var blog = Blog.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .contentHTML(request.getContentHTML())
                .contentJSON(request.getContentJSON())
                .thumbnailUrl(request.getThumbnailUrl())
                .bannerUrl(request.getBannerUrl())
                .slug(SlugGenerator.generateSlug(request.getTitle()))
                .tags(request.getTags())
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .status(resolveStatus(request.getStatus()))
                .publishedAt(resolvePublishedAt(null, resolveStatus(request.getStatus())))
                .createdAt(LocalDateTime.now())
                .blogCategory(blogCategory)
                .user(author)
                .build();

        return BlogResponse.fromEntity(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public BlogResponse updateBlog(UUID id, BlogRequest request) {
        var blog = blogRepository.getBlogById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        validateOwner(blog);
        validateUniqueTitleForUpdate(request.getTitle(), id);

        var blogCategory = getActiveBlogCategory(request.getBlogCategoryId());
        var targetStatus = resolveStatus(request.getStatus());

        if (blog.getStatus().equals(BlogStatus.ARCHIVED)) {
            // Admin revoking an archived blog back to draft or published => Reset deletedAt and deletedBy
            blog.setDeletedAt(null);
            blog.setDeletedBy(null);
        }

        blog.setTitle(request.getTitle());
        blog.setSlug(SlugGenerator.generateSlug(request.getTitle()));
        blog.setSummary(request.getSummary());
        blog.setContentJSON(request.getContentJSON());
        blog.setContentHTML(request.getContentHTML());
        blog.setThumbnailUrl(request.getThumbnailUrl());
        blog.setBannerUrl(request.getBannerUrl());
        blog.setTags(request.getTags());
        blog.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
        blog.setStatus(targetStatus);
        blog.setPublishedAt(resolvePublishedAt(blog.getPublishedAt(), targetStatus));
        blog.setBlogCategory(blogCategory);

        return BlogResponse.fromEntity(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public void deleteBlog(UUID id) {
        var blog = blogRepository.getBlogById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
        validateOwner(blog);
        var currentUser = getCurrentUserPrincipal();
        blog.setDeletedAt(LocalDateTime.now());
        blog.setDeletedBy(currentUser.getId());
        blog.setStatus(BlogStatus.ARCHIVED);
        blogRepository.save(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponse getBlogById(UUID id) {
        var blog = blogRepository.getBlogById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        validateBlogStatus(blog);
        return BlogResponse.fromEntity(blog);
    }

    @Override
    public BlogResponse getBlogBySlug(String slug) {
        var blog = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        validateBlogStatus(blog);
        return BlogResponse.fromEntity(blog);
    }

    private void validateBlogStatus(Blog blog) {
        if (blog.getStatus().equals(BlogStatus.ARCHIVED) || blog.getStatus().equals(BlogStatus.DRAFT)) {
            // Only allow owner or admin to view archived / draft blog
            var currentUser = getCurrentUserPrincipal();
            boolean isAdmin = currentUser.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
            if (!isAdmin) {
                throw new UnauthorizedException("You are not allowed to view this blog");
            }
        }
    }


    private UserPrincipal getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        if (!(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new UnauthorizedException("Invalid authenticated principal");
        }
        return userPrincipal;
    }

    private Blog getActiveBlogById(UUID id) {
        return blogRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
    }

    private void validateUniqueTitleForCreate(String title) {
        if (blogRepository.findByTitleAndDeletedAtIsNull(title).isPresent()) {
            throw new BadRequestException("Blog with the same title already exists");
        }
    }

    private void validateUniqueTitleForUpdate(String title, UUID blogId) {
        if (blogRepository.findByTitleAndIdNotAndDeletedAtIsNull(title, blogId).isPresent()) {
            throw new BadRequestException("Blog with the same title already exists");
        }
    }

    private User getCurrentUser() {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private BlogCategory getActiveBlogCategory(UUID categoryId) {
        var blogCategory = blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found"));
        if (blogCategory.getDeletedAt() != null) {
            throw new BadRequestException("Blog category is deleted");
        }
        return blogCategory;
    }

    private BlogStatus resolveStatus(BlogStatus status) {
        return status != null ? status : BlogStatus.DRAFT;
    }

    private LocalDateTime resolvePublishedAt(LocalDateTime currentPublishedAt, BlogStatus status) {
        if (status == BlogStatus.PUBLISHED) {
            return currentPublishedAt != null ? currentPublishedAt : LocalDateTime.now();
        }
        return null;
    }

    private void validateOwner(Blog blog) {
        var currentUser = getCurrentUserPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin && !blog.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not allowed to modify this blog");
        }
    }
}
