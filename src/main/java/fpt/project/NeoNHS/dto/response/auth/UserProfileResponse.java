package fpt.project.NeoNHS.dto.response.auth;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Data
@SuperBuilder
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String fullname;
    private String phoneNumber;
    private String avatarUrl;
    private String role;

    // Bank / payout info
    private String bankName;
    private String bankBin;
    private String bankAccountNumber;
    private String bankAccountName;
    private Double balance;
    // private Boolean isBankVerified;

    // KYC info
    private Boolean kycVerified;
    private String kycFullName;
    private String kycIdNumber;
}
