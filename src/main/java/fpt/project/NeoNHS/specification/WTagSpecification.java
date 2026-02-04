package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.entity.WTag;
import org.springframework.data.jpa.domain.Specification;

public class WTagSpecification {

    public static Specification<WTag> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<WTag> hasDescription(String description) {
        return (root, query, cb) -> {
            if (description == null || description.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }

    public static Specification<WTag> hasTagColor(String tagColor) {
        return (root, query, cb) -> {
            if (tagColor == null || tagColor.isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("tagColor"), tagColor);
        };
    }

    public static Specification<WTag> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }
}
