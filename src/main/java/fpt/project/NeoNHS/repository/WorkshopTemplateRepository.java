package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.WorkshopTemplate;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkshopTemplateRepository extends JpaRepository<WorkshopTemplate, UUID> {

    Page<WorkshopTemplate> findByVendorId(UUID vendorId, Pageable pageable);

    Page<WorkshopTemplate> findByStatus(WorkshopStatus status, Pageable pageable);

    Page<WorkshopTemplate> findByVendorIdAndStatus(UUID vendorId, WorkshopStatus status, Pageable pageable);

    List<WorkshopTemplate> findByVendorUserEmail(String email);
}
