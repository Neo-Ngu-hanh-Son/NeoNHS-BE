package fpt.project.NeoNHS.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VendorRegisterRequest {
    // User info
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-zA-Z]).*$", message = "Password must contain at least one letter and one number")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
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
