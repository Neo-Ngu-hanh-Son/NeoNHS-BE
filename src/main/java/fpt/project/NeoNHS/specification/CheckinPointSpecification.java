package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.entity.CheckinPoint;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckinPointSpecification {
    public static Specification<CheckinPoint> withFilters(UUID pointId, String search, boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Optional pointId filter
            if (pointId != null) {
                predicates.add(criteriaBuilder.equal(root.get("point").get("id"), pointId));
            }

            // active => deletedAt == null
            if (isActive) {
                predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            }

            // Keyword search by name
            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
