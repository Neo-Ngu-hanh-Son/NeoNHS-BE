package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Blog;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying
    @Transactional
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + :views WHERE b.id = :id")
    void incrementViews(@Param("id") UUID id, @Param("views") int views);
}
