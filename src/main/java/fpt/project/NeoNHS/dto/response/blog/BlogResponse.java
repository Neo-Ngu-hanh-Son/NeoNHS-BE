package fpt.project.NeoNHS.dto.response.blog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fpt.project.NeoNHS.dto.response.auth.UserInfoResponse;
import fpt.project.NeoNHS.entity.Blog;
import fpt.project.NeoNHS.enums.BlogStatus;
import jakarta.persistence.Column;
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
    private String contentJSON; // Use for lexical content (mobile and web)
    private String contentHTML; // Use for mobile render
    private String thumbnailUrl;
    private String bannerUrl;
    private Boolean isFeatured;
    private BlogStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String tags;
    private Integer viewCount;
    private BlogCategoryResponse blogCategory;
    private UserInfoResponse user;

    public static BlogResponse fromEntity(Blog blog) {
        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .summary(blog.getSummary())
                .contentHTML(blog.getContentHTML())
                .contentJSON(blog.getContentJSON())
                .thumbnailUrl(blog.getThumbnailUrl())
                .bannerUrl(blog.getBannerUrl())
                .isFeatured(blog.getIsFeatured())
                .status(blog.getStatus())
                .publishedAt(blog.getPublishedAt())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .tags(blog.getTags())
                .viewCount(blog.getViewCount())
                .blogCategory(BlogCategoryResponse.fromEntity(blog.getBlogCategory()))
                .user(UserInfoResponse.fromEntity(blog.getUser()))
                .build();
    }
}
