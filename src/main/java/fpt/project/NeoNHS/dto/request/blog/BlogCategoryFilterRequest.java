package fpt.project.NeoNHS.dto.request.blog;

import fpt.project.NeoNHS.constants.PaginationConstants;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class BlogCategoryFilterRequest {
    int page;
    int size;
    String name;
    String UUID;
    String sortBy;
    String sortDir;
}
