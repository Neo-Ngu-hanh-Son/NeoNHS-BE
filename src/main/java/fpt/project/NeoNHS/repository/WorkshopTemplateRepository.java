package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.WorkshopTemplate;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkshopTemplateRepository
        extends JpaRepository<WorkshopTemplate, UUID> {

    List<WorkshopTemplate> findByVendorId(UUID vendorId);

    List<WorkshopTemplate> findByStatus(WorkshopStatus status);

    List<WorkshopTemplate> findByVendorIdAndStatus(UUID vendorId, WorkshopStatus status);

    List<WorkshopTemplate> findByVendorUserEmail(String email);
}
