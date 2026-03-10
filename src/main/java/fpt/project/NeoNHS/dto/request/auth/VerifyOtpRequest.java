package fpt.project.NeoNHS.dto.request.auth;

import lombok.Data;

@Data
public class VerifyOtpRequest {
  private String email;
  private String otp;
}
