package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.auth.UpdateVendorProfileRequest;
import fpt.project.NeoNHS.dto.request.auth.VendorRegisterRequest;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;

import java.util.UUID;

public interface VendorProfileService {
    VendorProfileResponse createVendorAccount(VendorRegisterRequest request); // Thêm mới
    VendorProfileResponse getVendorProfile(String email);
    VendorProfileResponse updateVendorProfile(UUID id, String email, UpdateVendorProfileRequest request);

}
