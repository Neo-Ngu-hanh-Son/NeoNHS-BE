package fpt.project.NeoNHS.dto.response.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorProfileResponse extends UserInfoResponse {
    private String businessName;
    private String description;
    private String address;
    private String taxCode;
    private boolean isVerifiedVendor;
}
