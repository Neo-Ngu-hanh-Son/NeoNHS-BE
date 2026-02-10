package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.dto.request.event.EventFilterRequest;
import fpt.project.NeoNHS.entity.Event;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> withFilters(EventFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Soft delete filter logic:
            // - If includeDeleted = true: show all events (ignore deleted filter)
            // - If deleted = true: show only deleted events (deletedAt IS NOT NULL)
            // - If deleted = false or null: show only active events (deletedAt IS NULL) - default behavior
            if (!Boolean.TRUE.equals(filter.getIncludeDeleted())) {
                if (Boolean.TRUE.equals(filter.getDeleted())) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("deletedAt")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
                }
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
            }

            if (filter.getLocation() != null && !filter.getLocation().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("locationName")),
                        "%" + filter.getLocation().toLowerCase() + "%"
                ));
            }

            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("startTime"),
                        filter.getStartDate()
                ));
            }

            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("endTime"),
                        filter.getEndDate()
                ));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        filter.getMinPrice()
                ));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        filter.getMaxPrice()
                ));
            }

            // Filter by tags (OR logic: event has at least one of the specified tags)
            if (filter.getTagIds() != null && !filter.getTagIds().isEmpty()) {
                Join<Object, Object> eventTagJoin = root.join("eventTags", JoinType.INNER);
                predicates.add(eventTagJoin.get("eTag").get("id").in(filter.getTagIds()));
                
                // Use distinct to avoid duplicate results when an event has multiple matching tags
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
