package fpt.project.NeoNHS.dto.request.blog;

import java.util.UUID;

import fpt.project.NeoNHS.enums.BlogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequest {
  @NotBlank(message = "Title is required")
  private String title;
  private String slug;
  private String summary;

  @NotBlank(message = "Content is required")
  private String content;

  private String thumbnailUrl;

  private String bannerUrl;

  @Builder.Default
  private Boolean isFeatured = false;

  @Builder.Default
  private BlogStatus status = BlogStatus.DRAFT;

  private String tags;

  @NotNull(message = "Blog category is required")
  private UUID blogCategoryId;
}
