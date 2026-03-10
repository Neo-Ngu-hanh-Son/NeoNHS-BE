package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.admin.BanVendorRequest;
import fpt.project.NeoNHS.dto.request.admin.CreateVendorByAdminRequest;
import fpt.project.NeoNHS.dto.request.admin.UpdateVendorByAdminRequest;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminVendorManagementService {

    /**
     * Create a new vendor account by admin
     */
    VendorProfileResponse createVendorByAdmin(CreateVendorByAdminRequest request);

    /**
     * Get all vendors with pagination
     */
    Page<VendorProfileResponse> getAllVendors(Pageable pageable);

    /**
     * Get vendor by ID
     */
    VendorProfileResponse getVendorById(UUID id);

    /**
     * Update vendor profile by admin
     */
    VendorProfileResponse updateVendorByAdmin(UUID id, UpdateVendorByAdminRequest request);

    /**
     * Ban a vendor account
     */
    VendorProfileResponse banVendor(UUID id, BanVendorRequest request);

    /**
     * Unban a vendor account
     */
    VendorProfileResponse unbanVendor(UUID id);

    /**
     * Delete vendor account (soft delete)
     */
    void deleteVendor(UUID id);

    /**
     * Search vendors by keyword (name, email, business name)
     */
    Page<VendorProfileResponse> searchVendors(String keyword, Pageable pageable);

    /**
     * Filter vendors by verification status
     */
    Page<VendorProfileResponse> filterVendorsByVerification(Boolean isVerified, Pageable pageable);

    /**
     * Filter vendors by banned status
     */
    Page<VendorProfileResponse> filterVendorsByBannedStatus(Boolean isBanned, Pageable pageable);

    /**
     * Filter vendors by active status
     */
    Page<VendorProfileResponse> filterVendorsByActiveStatus(Boolean isActive, Pageable pageable);

    /**
     * Advanced search and filter
     */
    Page<VendorProfileResponse> advancedSearchAndFilter(
            String keyword,
            Boolean isVerified,
            Boolean isBanned,
            Boolean isActive,
            Pageable pageable
    );
}
