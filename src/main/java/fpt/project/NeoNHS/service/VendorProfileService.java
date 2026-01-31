package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.UpdateVendorProfileRequest;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;

public interface VendorProfileService {
    VendorProfileResponse getVendorProfile(String email);
    VendorProfileResponse updateVendorProfile(String email, UpdateVendorProfileRequest request);
}
