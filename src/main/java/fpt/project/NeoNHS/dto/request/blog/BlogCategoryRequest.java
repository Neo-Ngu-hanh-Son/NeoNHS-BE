package fpt.project.NeoNHS.dto.request.blog;

import fpt.project.NeoNHS.enums.BlogCategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlogCategoryRequest {
  @NotBlank(message = "Category name is required")
  private String name;

  private String description;

  @NotNull(message = "Category status is required")
  private BlogCategoryStatus status;
}
