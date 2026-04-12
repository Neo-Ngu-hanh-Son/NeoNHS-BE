package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.dto.request.admin.ReportFilterRequest;
import fpt.project.NeoNHS.entity.Report;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ReportSpecification {
    public static Specification<Report> withFilters(ReportFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getTargetType() != null && !filter.getTargetType().equalsIgnoreCase("ALL")) {
                predicates.add(cb.equal(cb.upper(root.get("targetType")), filter.getTargetType().toUpperCase()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getReporterName() != null && !filter.getReporterName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("reporter").get("fullname")),
                        "%" + filter.getReporterName().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}