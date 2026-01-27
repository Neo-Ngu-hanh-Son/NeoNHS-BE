package fpt.project.NeoNHS.dto.response;

import fpt.project.NeoNHS.dto.response.auth.UserInfoResponse;
import fpt.project.NeoNHS.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserInfoResponse userInfo;
}