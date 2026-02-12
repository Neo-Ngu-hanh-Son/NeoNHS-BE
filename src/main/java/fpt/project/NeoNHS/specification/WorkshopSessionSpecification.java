package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.enums.SessionStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WorkshopSessionSpecification {

    // ==================== SESSION-SPECIFIC FILTERS ====================

    public static Specification<WorkshopSession> hasStatus(SessionStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<WorkshopSession> hasStartTimeAfter(LocalDateTime startDate) {
        return (root, query, cb) -> {
            if (startDate == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("startTime"), startDate);
        };
    }

    public static Specification<WorkshopSession> hasStartTimeBefore(LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (endDate == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("startTime"), endDate);
        };
    }

    public static Specification<WorkshopSession> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }
            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get("price"), minPrice, maxPrice);
            }
            if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<WorkshopSession> hasAvailableSlots() {
        return (root, query, cb) -> cb.lessThan(root.get("currentEnrolled"), root.get("maxParticipants"));
    }

    // ==================== TEMPLATE-RELATED FILTERS ====================

    public static Specification<WorkshopSession> hasTemplateId(UUID templateId) {
        return (root, query, cb) -> {
            if (templateId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("workshopTemplate").get("id"), templateId);
        };
    }

    public static Specification<WorkshopSession> hasVendorId(UUID vendorId) {
        return (root, query, cb) -> {
            if (vendorId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("workshopTemplate").get("vendor").get("id"), vendorId);
        };
    }

    public static Specification<WorkshopSession> hasTagId(UUID tagId) {
        return (root, query, cb) -> {
            if (tagId == null) {
                return cb.conjunction();
            }
            var templateJoin = root.join("workshopTemplate", JoinType.INNER);
            var workshopTagJoin = templateJoin.join("workshopTags", JoinType.INNER);
            return cb.equal(workshopTagJoin.get("wTag").get("id"), tagId);
        };
    }

    public static Specification<WorkshopSession> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            var templateJoin = root.join("workshopTemplate", JoinType.INNER);
            return cb.or(
                    cb.like(cb.lower(templateJoin.get("name")), pattern),
                    cb.like(cb.lower(templateJoin.get("shortDescription")), pattern),
                    cb.like(cb.lower(templateJoin.get("fullDescription")), pattern)
            );
        };
    }
}
