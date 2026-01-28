package fpt.project.NeoNHS.dto.response.auth;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class VendorProfileResponse extends UserProfileResponse {
    private String businessName;
    private String description;
    private String address;
    private String latitude;
    private String longitude;
    private String taxCode;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private Boolean isVerifiedVendor;
}
