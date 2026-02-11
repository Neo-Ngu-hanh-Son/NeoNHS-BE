package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.entity.BlogCategory;
import fpt.project.NeoNHS.enums.BlogCategoryStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BlogCategorySpecification {

  public static Specification<BlogCategory> withFilters(String search, BlogCategoryStatus status) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (status != null) {
        if (status == BlogCategoryStatus.ACTIVE) {
          predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
        } else if (status == BlogCategoryStatus.ARCHIVED) {
          predicates.add(criteriaBuilder.isNotNull(root.get("deletedAt")));
        }
      }

      if (search != null && !search.isBlank()) {
        predicates.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get("name")),
            "%" + search.toLowerCase() + "%"));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
