package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, UUID>, JpaSpecificationExecutor<Attraction> {
    // Tìm kiếm theo tên (không phân biệt hoa thường) và chỉ lấy những cái đang active
    @Query("SELECT a FROM Attraction a WHERE a.isActive = true " +
            "AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Attraction> findAllActive(String search, Pageable pageable);
}
