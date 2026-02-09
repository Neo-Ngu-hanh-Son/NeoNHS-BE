package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.entity.WorkshopTemplate;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class WorkshopTemplateSpecification {

    public static Specification<WorkshopTemplate> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<WorkshopTemplate> hasStatus(WorkshopStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<WorkshopTemplate> hasVendorId(UUID vendorId) {
        return (root, query, cb) -> {
            if (vendorId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("vendor").get("id"), vendorId);
        };
    }

    public static Specification<WorkshopTemplate> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }
            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get("defaultPrice"), minPrice, maxPrice);
            }
            if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get("defaultPrice"), minPrice);
            }
            return cb.lessThanOrEqualTo(root.get("defaultPrice"), maxPrice);
        };
    }

    public static Specification<WorkshopTemplate> hasDurationBetween(Integer minDuration, Integer maxDuration) {
        return (root, query, cb) -> {
            if (minDuration == null && maxDuration == null) {
                return cb.conjunction();
            }
            if (minDuration != null && maxDuration != null) {
                return cb.between(root.get("estimatedDuration"), minDuration, maxDuration);
            }
            if (minDuration != null) {
                return cb.greaterThanOrEqualTo(root.get("estimatedDuration"), minDuration);
            }
            return cb.lessThanOrEqualTo(root.get("estimatedDuration"), maxDuration);
        };
    }

    public static Specification<WorkshopTemplate> hasMinRating(BigDecimal minRating) {
        return (root, query, cb) -> {
            if (minRating == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("averageRating"), minRating);
        };
    }

    public static Specification<WorkshopTemplate> hasTagId(UUID tagId) {
        return (root, query, cb) -> {
            if (tagId == null) {
                return cb.conjunction();
            }
            var workshopTagJoin = root.join("workshopTags", JoinType.INNER);
            return cb.equal(workshopTagJoin.get("wTag").get("id"), tagId);
        };
    }

    public static Specification<WorkshopTemplate> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("shortDescription")), pattern),
                    cb.like(cb.lower(root.get("fullDescription")), pattern));
        };
    }
}
