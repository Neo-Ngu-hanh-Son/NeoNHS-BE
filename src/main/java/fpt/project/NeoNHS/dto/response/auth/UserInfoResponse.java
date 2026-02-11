package fpt.project.NeoNHS.dto.response.auth;

import fpt.project.NeoNHS.entity.User;
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

    public static UserInfoResponse fromEntity(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .isBanned(user.getIsBanned())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
