package fpt.project.NeoNHS.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fpt.project.NeoNHS.dto.request.blog.BlogRequest;
import fpt.project.NeoNHS.dto.response.blog.BlogResponse;
import fpt.project.NeoNHS.enums.BlogStatus;

public interface BlogService {

    Page<BlogResponse> getBlogs(String search, BlogStatus status, List<String> tags, Pageable pageable,
            boolean featured, String categorySlug);

    BlogResponse createBlog(BlogRequest request);

    BlogResponse updateBlog(UUID id, BlogRequest request);

    void deleteBlog(UUID id);

    BlogResponse getBlogById(UUID id);

    BlogResponse getBlogBySlug(String slug);

}
