package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.VendorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, UUID> {

    Optional<VendorProfile> findByUserEmail(String email);
}
