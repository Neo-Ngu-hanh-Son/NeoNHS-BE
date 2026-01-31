package fpt.project.NeoNHS.dto.response.auth;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Data
@SuperBuilder
public class UserProfileResponse {
    private UUID id;//tra ve id lam gi nhi ? thoi ke de day di
    private String email;
    private String fullname;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
}
