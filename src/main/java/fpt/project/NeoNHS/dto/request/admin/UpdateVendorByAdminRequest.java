package fpt.project.NeoNHS.dto.request.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVendorByAdminRequest {

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

    private Boolean isVerified;

    private Boolean isActive;
}
