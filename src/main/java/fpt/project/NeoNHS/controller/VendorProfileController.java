package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.UpdateVendorProfileRequest;
import fpt.project.NeoNHS.dto.request.VendorRegisterRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;
import fpt.project.NeoNHS.service.VendorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorProfileController {

    private final VendorProfileService vendorProfileService;

    @PostMapping("/register")
    public ApiResponse<VendorProfileResponse> registerVendor(@RequestBody VendorRegisterRequest request) {
        VendorProfileResponse data = vendorProfileService.createVendorAccount(request);
        return ApiResponse.success(HttpStatus.CREATED, "Vendor account created successfully", data);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> getMyBusinessProfile(Principal principal) {
        VendorProfileResponse data = vendorProfileService.getVendorProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Vendor profile retrieved", data));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> updateMyBusinessProfile(
            Principal principal,
            @RequestBody UpdateVendorProfileRequest request) {

        VendorProfileResponse data = vendorProfileService.updateVendorProfile(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Vendor profile updated successfully", data));
    }
}
