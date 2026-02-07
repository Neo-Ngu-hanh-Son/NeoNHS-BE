package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.auth.UpdateVendorProfileRequest;
import fpt.project.NeoNHS.dto.request.auth.VendorRegisterRequest;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;

public interface VendorProfileService {
    VendorProfileResponse createVendorAccount(VendorRegisterRequest request); // Thêm mới
    VendorProfileResponse getVendorProfile(String email);
    VendorProfileResponse updateVendorProfile(String email, UpdateVendorProfileRequest request);
}
