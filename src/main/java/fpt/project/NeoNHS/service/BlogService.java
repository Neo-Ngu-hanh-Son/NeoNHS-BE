package fpt.project.NeoNHS.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fpt.project.NeoNHS.dto.request.blog.BlogRequest;
import fpt.project.NeoNHS.dto.response.blog.BlogResponse;
import fpt.project.NeoNHS.enums.BlogStatus;
import org.springframework.transaction.annotation.Transactional;

public interface BlogService {

    Page<BlogResponse> getBlogs(String search, BlogStatus status, List<String> tags, Pageable pageable,
            boolean featured, String categorySlug);

    @Transactional(readOnly = true)
    Page<BlogResponse> getActiveBlogs(String search, BlogStatus status, List<String> tags, Pageable pageable,
                                      boolean featured, String categorySlug);

    @Transactional(readOnly = true)
    Page<BlogResponse> getActiveBlogsPreview(String search, BlogStatus status, List<String> tags, Pageable pageable,
                                             boolean featured, String categorySlug);

    BlogResponse createBlog(BlogRequest request);

    BlogResponse updateBlog(UUID id, BlogRequest request);

    void deleteBlog(UUID id);

    void deleteBlogHard(UUID id);

    void emptyAllDeletedBlogs();

    BlogResponse getBlogById(UUID id);

    BlogResponse getBlogBySlug(String slug);

    void incrementViewCount(UUID id);
    void addTotalViewCount(UUID id, int viewsToAdd);
}
