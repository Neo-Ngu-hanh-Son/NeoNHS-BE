package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.blog.BlogCategoryRequest;
import fpt.project.NeoNHS.dto.response.blog.BlogCategoryResponse;
import fpt.project.NeoNHS.enums.BlogCategoryStatus;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BlogCategoryService {

  Page<BlogCategoryResponse> getBlogCategories(String search, BlogCategoryStatus status, Pageable pageable);

  void deleteBlogCategory(UUID categoryId);

  void createBlogCategory(BlogCategoryRequest request);

  void updateBlogCategory(UUID categoryId, BlogCategoryRequest request);
}
