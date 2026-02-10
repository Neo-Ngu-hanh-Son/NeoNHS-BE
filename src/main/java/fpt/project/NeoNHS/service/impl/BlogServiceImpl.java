package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.blog.BlogRequest;
import fpt.project.NeoNHS.dto.response.blog.BlogResponse;
import fpt.project.NeoNHS.enums.BlogStatus;
import fpt.project.NeoNHS.repository.BlogRepository;
import fpt.project.NeoNHS.service.BlogService;
import fpt.project.NeoNHS.specification.BlogSpecification;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;

    @Override
    public Page<BlogResponse> getBlogs(String search, BlogStatus status, List<String> tags, Pageable pageable) {
        var spec = BlogSpecification.withFilters(search, status, tags);
        return blogRepository.findAll(spec, pageable).map(BlogResponse::fromEntity);
    }

    @Override
    public BlogResponse createBlog(BlogRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createBlog'");
    }

    @Override
    public BlogResponse updateBlog(UUID id, BlogRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateBlog'");
    }

    @Override
    public void deleteBlog(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteBlog'");
    }

    @Override
    public BlogResponse getBlogById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBlogById'");
    }
}
