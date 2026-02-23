package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.entity.Blog;
import fpt.project.NeoNHS.enums.BlogStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BlogSpecification {

    public static Specification<Blog> withFilters(String search, BlogStatus status, List<String> tags) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            } else {
                // status == null => Show all but archived
                predicates.add(criteriaBuilder.notEqual(root.get("status"), BlogStatus.ARCHIVED));
            }

            if (search != null && !search.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + search.toLowerCase() + "%"));
            }

            // Check if tags is in the blog tags (All tags from request must be present in
            // blog tags)
            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    if (StringUtils.hasText(tag)) {
                        predicates.add(criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("tags")),
                                "%" + tag.toLowerCase() + "%"));
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
