package fpt.project.NeoNHS.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class UpdateVendorProfileRequest {
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
