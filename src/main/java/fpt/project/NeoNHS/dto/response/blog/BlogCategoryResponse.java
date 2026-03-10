package fpt.project.NeoNHS.dto.response.blog;

import fpt.project.NeoNHS.entity.BlogCategory;
import fpt.project.NeoNHS.enums.BlogCategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCategoryResponse {
  private UUID id;
  private String name;
  private String slug;
  private String description;
  private BlogCategoryStatus status;
  private long postCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static BlogCategoryResponse fromEntity(BlogCategory blogCategory) {
    return BlogCategoryResponse.builder()
        .id(blogCategory.getId())
        .name(blogCategory.getName())
        .slug(blogCategory.getSlug())
        .description(blogCategory.getDescription())
        .status(blogCategory.getStatus())
        .postCount(blogCategory.getBlogs().size())
        .createdAt(blogCategory.getCreatedAt())
        .updatedAt(blogCategory.getUpdatedAt())
        .build();
  }
}
