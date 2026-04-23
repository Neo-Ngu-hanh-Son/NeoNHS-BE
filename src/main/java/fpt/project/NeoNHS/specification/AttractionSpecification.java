package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.dto.request.attraction.AttractionFilterRequest;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.Point;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AttractionSpecification {
    public static Specification<Attraction> withFilters(AttractionFilterRequest filter, boolean activeOnly) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Handle Spring Boot pagination related stuff (Because it return a long when it is counting, and we don't want to perform
            // the distinct on the count number
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }
            if (activeOnly) {
                predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
                predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getName() != null && !filter.getName().isBlank()) {
                String keyword = "%" + filter.getName().toLowerCase() + "%";
                Join<Attraction, Point> pointsJoin = root.join("points", JoinType.LEFT);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(pointsJoin.get("name")), keyword)
                ));
            }
//            This old code is using AND for name, description, and address, which is too strict.
//            The above new code uses OR to allow matching any of the fields.
//            if (filter.getName() != null && !filter.getName().isBlank()) {
//                predicates.add(criteriaBuilder.like(
//                        criteriaBuilder.lower(root.get("name")),
//                        "%" + filter.getName().toLowerCase() + "%"
//                ));
//            }
//
//            if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
//                predicates.add(criteriaBuilder.like(
//                        criteriaBuilder.lower(root.get("description")),
//                        "%" + filter.getDescription().toLowerCase() + "%"
//                ));
//            }
//
//            if (filter.getAddress() != null && !filter.getAddress().isBlank()) {
//                predicates.add(criteriaBuilder.like(
//                        criteriaBuilder.lower(root.get("address")),
//                        "%" + filter.getAddress().toLowerCase() + "%"
//                ));
//            }

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
