package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.blog.BlogCategoryResponse;
import fpt.project.NeoNHS.enums.BlogCategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogCategoryService {

  Page<BlogCategoryResponse> getBlogCategories(String search, BlogCategoryStatus status, Pageable pageable);
}
