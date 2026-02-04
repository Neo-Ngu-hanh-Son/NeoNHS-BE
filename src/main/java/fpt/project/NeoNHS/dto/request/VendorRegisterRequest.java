package fpt.project.NeoNHS.dto.request;

import lombok.Data;

@Data
public class VendorRegisterRequest {
    // User info
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;

    // Vendor profile info
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
