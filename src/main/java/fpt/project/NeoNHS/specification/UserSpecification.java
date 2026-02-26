package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.dto.request.user.UserFilterRequest;
import fpt.project.NeoNHS.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> withFilters(UserFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Logic Soft Delete:
            // - Nếu includeDeleted = true: hiện tất cả (bỏ qua lọc deletedAt)
            // - Nếu deleted = true: chỉ hiện user đã xóa (deletedAt IS NOT NULL)
            // - Mặc định: chỉ hiện user chưa xóa (deletedAt IS NULL)
            if (!Boolean.TRUE.equals(filter.getIncludeDeleted())) {
                if (Boolean.TRUE.equals(filter.getDeleted())) {
                    predicates.add(cb.isNotNull(root.get("deletedAt")));
                } else {
                    predicates.add(cb.isNull(root.get("deletedAt")));
                }
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String pattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullname")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("phoneNumber")), pattern)
                ));
            }

            if (filter.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), filter.getRole()));
            }

            if (filter.getIsBanned() != null) {
                predicates.add(cb.equal(root.get("isBanned"), filter.getIsBanned()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}