package fpt.project.NeoNHS.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String oldPassword;
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String newPassword;
    private String confirmNewPassword;
}