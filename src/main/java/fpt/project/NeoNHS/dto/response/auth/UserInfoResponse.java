package fpt.project.NeoNHS.dto.response.auth;

import fpt.project.NeoNHS.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
public class UserInfoResponse {
    private UUID id;
    private String fullname;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private UserRole role;
    private Boolean isActive = true;
    private Boolean isVerified = false;
    private Boolean isBanned = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
