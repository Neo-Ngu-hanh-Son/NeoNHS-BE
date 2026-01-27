package fpt.project.NeoNHS.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyEmailRequest {

    @Email(message = "INVALID_EMAIL_FORMAT")
    @NotBlank(message = "BLANK_EMAIL")
    private String email;

    @NotBlank(message = "INVALID_VERIFICATION_CODE")
    private String verificationCode;
}
