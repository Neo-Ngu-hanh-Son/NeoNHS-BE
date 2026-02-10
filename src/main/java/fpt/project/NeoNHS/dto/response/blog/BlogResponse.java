package fpt.project.NeoNHS.dto.response.blog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fpt.project.NeoNHS.dto.response.auth.UserInfoResponse;
import fpt.project.NeoNHS.entity.Blog;
import fpt.project.NeoNHS.enums.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponse {
  private UUID id;
  private String title;
  private String slug;
  private String summary;
  private String content;

  private String thumbnailUrl;

  private String bannerUrl;
  private Boolean isFeatured;
  private BlogStatus status;
  private LocalDateTime publishedAt;
  private String tags;
  private Integer viewCount = 0;
  private BlogCategoryResponse blogCategory;
  private UserInfoResponse user;

  public static BlogResponse fromEntity(Blog blog) {
    return BlogResponse.builder()
        .id(blog.getId())
        .title(blog.getTitle())
        .slug(blog.getSlug())
        .summary(blog.getSummary())
        .content(blog.getContent())
        .thumbnailUrl(blog.getThumbnailUrl())
        .bannerUrl(blog.getBannerUrl())
        .isFeatured(blog.getIsFeatured())
        .status(blog.getStatus())
        .publishedAt(blog.getPublishedAt())
        .tags(blog.getTags())
        .viewCount(blog.getViewCount())
        .blogCategory(BlogCategoryResponse.fromEntity(blog.getBlogCategory()))
        .user(UserInfoResponse.fromEntity(blog.getUser()))
        .build();
  }
}
