package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.dto.request.voucher.VoucherFilterRequest;
import fpt.project.NeoNHS.entity.Voucher;
import fpt.project.NeoNHS.enums.VoucherStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VoucherSpecification {

    public static Specification<Voucher> withFilters(VoucherFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!Boolean.TRUE.equals(filter.getIncludeDeleted())) {
                if (Boolean.TRUE.equals(filter.getDeleted())) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("deletedAt")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
                }
            }

            if (filter.getScope() != null) {
                predicates.add(criteriaBuilder.equal(root.get("scope"), filter.getScope()));
            }

            if (filter.getVoucherType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("voucherType"), filter.getVoucherType()));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getApplicableProduct() != null) {
                predicates.add(criteriaBuilder.equal(root.get("applicableProduct"), filter.getApplicableProduct()));
            }

            if (filter.getCode() != null && !filter.getCode().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        "%" + filter.getCode().toLowerCase() + "%"
                ));
            }

            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("startDate"),
                        filter.getStartDate().atStartOfDay()
                ));
            }

            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("endDate"),
                        filter.getEndDate().atTime(LocalTime.MAX)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter only ACTIVE and available vouchers (not expired, under usage limit).
     */
    public static Specification<Voucher> withAvailableFilters(VoucherFilterRequest filter, LocalDateTime now) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base Availability Logic
            predicates.add(criteriaBuilder.equal(root.get("status"), VoucherStatus.ACTIVE));
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            // (v.startDate IS NULL OR v.startDate <= :now)
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("startDate")),
                    criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), now)
            ));

            // (v.endDate IS NULL OR v.endDate >= :now)
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("endDate")),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), now)
            ));

            // (v.usageLimit IS NULL OR v.usageCount < v.usageLimit)
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("usageLimit")),
                    criteriaBuilder.lessThan(root.get("usageCount"), root.get("usageLimit"))
            ));

            // Additional filters from request DTO
            if (filter.getScope() != null) {
                predicates.add(criteriaBuilder.equal(root.get("scope"), filter.getScope()));
            }

            if (filter.getVoucherType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("voucherType"), filter.getVoucherType()));
            }

            if (filter.getApplicableProduct() != null) {
                predicates.add(criteriaBuilder.equal(root.get("applicableProduct"), filter.getApplicableProduct()));
            }

            if (filter.getCode() != null && !filter.getCode().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        "%" + filter.getCode().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter available vouchers for a specific vendor.
     */
    public static Specification<Voucher> withAvailableVendorFilters(UUID vendorId, VoucherFilterRequest filter, LocalDateTime now) {
        return Specification.where(withAvailableFilters(filter, now))
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("vendor").get("id"), vendorId));
    }

    /**
     * Filter vouchers for a specific vendor (by vendor_id) with additional filters (Admin/Vendor view).
     */
    public static Specification<Voucher> withVendorFilters(UUID vendorId, VoucherFilterRequest filter) {
        return Specification.where(withFilters(filter))
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("vendor").get("id"), vendorId));
    }
}
