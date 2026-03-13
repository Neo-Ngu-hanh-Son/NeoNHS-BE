package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.entity.Point;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PointSpecification {
    public static Specification<Point> withFilters(String search, boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // isActive = true => only return points that are not deleted (deletedAt is null)
            if (isActive) {
                predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            }

            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
