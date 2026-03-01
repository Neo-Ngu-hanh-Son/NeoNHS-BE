package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Blog;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogRepository extends JpaRepository<Blog, UUID>, JpaSpecificationExecutor<Blog> {
    long countByBlogCategoryId(UUID blogCategoryId);

    Optional<Blog> findByTitle(String title);

    Optional<Blog> findByTitleAndIdNot(String title, UUID id);

    Optional<Blog> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Blog> findByTitleAndDeletedAtIsNull(String title);

    Optional<Blog> findByTitleAndIdNotAndDeletedAtIsNull(String title, UUID id);

    Optional<Blog> getBlogById(UUID id);

    Optional<Blog> findBySlugIgnoreCase(String slug);

    long countByDeletedAtIsNull();
}
