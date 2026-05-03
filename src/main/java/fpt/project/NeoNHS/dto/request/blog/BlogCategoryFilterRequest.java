package fpt.project.NeoNHS.dto.request.blog;

import lombok.Data;

@Data
public class BlogCategoryFilterRequest {
    int page;
    int size;
    String name;
    String UUID;
    String sortBy;
    String sortDir;
}
