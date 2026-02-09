package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.dto.request.attraction.AttractionFilterRequest;
import fpt.project.NeoNHS.dto.request.event.EventFilterRequest;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.Event;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AttractionSpecification {
    public static Specification<Attraction> withFilters(AttractionFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
            }

            if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + filter.getDescription().toLowerCase() + "%"
                ));
            }

            if (filter.getAddress() != null && !filter.getAddress().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("address")),
                        "%" + filter.getAddress().toLowerCase() + "%"
                ));
            }

            if (filter.getOpenHour() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("openHour"),
                        filter.getOpenHour()
                ));
            }

            if (filter.getCloseHour() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("closeHour"),
                        filter.getCloseHour()
                ));
            }


            if (filter.getLatitude() != null && filter.getLongitude() != null) {

                Double userLat = filter.getLatitude().doubleValue();
                Double userLon = filter.getLongitude().doubleValue();

                Expression<Double> distanceMeters = criteriaBuilder.function(
                        "ST_Distance_Sphere",
                        Double.class,
                        criteriaBuilder.function(
                                "POINT",
                                Object.class,
                                root.get("longitude"),
                                root.get("latitude")
                        ),
                        criteriaBuilder.function(
                                "POINT",
                                Object.class,
                                criteriaBuilder.literal(userLon),
                                criteriaBuilder.literal(userLat)
                        )
                );

                predicates.add(criteriaBuilder.lessThanOrEqualTo(distanceMeters, 100.0));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
