package fpt.project.NeoNHS.dto.response.user;

import fpt.project.NeoNHS.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String fullname;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private UserRole role;
    private Boolean isActive;
    private Boolean isVerified;
    private Boolean isBanned;
    private String banReason;
    private LocalDateTime bannedAt;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(fpt.project.NeoNHS.entity.User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .isBanned(user.getIsBanned())
                .banReason(user.getBanReason())
                .bannedAt(user.getBannedAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}