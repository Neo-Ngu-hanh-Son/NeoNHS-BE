package fpt.project.NeoNHS.dto.response.auth;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public class VendorProfileResponse extends UserProfileResponse {
    private UUID id;
    private UUID userId;
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
