package fpt.project.NeoNHS.dto.request.auth;

import lombok.Data;

@Data
public class UpdateVendorProfileRequest {
    private String fullname;
    private String phoneNumber;
    private String avatarUrl;
    private String businessName;
    private String description;
    private String address;
    private String latitude;
    private String longitude;
    private String taxCode;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
}
