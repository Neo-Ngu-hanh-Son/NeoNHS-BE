package fpt.project.NeoNHS.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVendorByAdminRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullname;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).+$",
            message = "Password must contain at least one letter and one number")
    private String password;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 200, message = "Business name must be between 2 and 200 characters")
    private String businessName;

    private String description;

    private String address;

    private String latitude;

    private String longitude;

    private String taxCode;

    private String bankName;

    private String bankAccountNumber;

    private String bankAccountName;

    @Builder.Default
    private Boolean isVerified = true;
}
