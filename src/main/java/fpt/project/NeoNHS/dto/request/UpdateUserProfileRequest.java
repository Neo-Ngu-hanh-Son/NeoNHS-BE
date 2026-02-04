package fpt.project.NeoNHS.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateUserProfileRequest {
    private UUID id;
    private String fullname;

    @Pattern(regexp = "^0[0-9]{9}$", message = "Phone must be 10 digits starting with 0")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String avatarUrl;
}
