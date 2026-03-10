package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.dto.request.voucher.VoucherFilterRequest;
import fpt.project.NeoNHS.entity.Voucher;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class VoucherSpecification {

    public static Specification<Voucher> withFilters(VoucherFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Soft delete filter logic:
            // - If includeDeleted = true: show all vouchers (ignore deleted filter)
            // - If deleted = true: show only deleted vouchers (deletedAt IS NOT NULL)
            // - If deleted = false or null: show only active vouchers (deletedAt IS NULL) - default
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
     * Filter vouchers for a specific vendor (by vendor_id) with additional filters.
     */
    public static Specification<Voucher> withVendorFilters(java.util.UUID vendorId, VoucherFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by vendor
            predicates.add(criteriaBuilder.equal(root.get("vendor").get("id"), vendorId));

            // Soft delete: vendors only see non-deleted
            if (!Boolean.TRUE.equals(filter.getIncludeDeleted())) {
                if (Boolean.TRUE.equals(filter.getDeleted())) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("deletedAt")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
                }
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
}
