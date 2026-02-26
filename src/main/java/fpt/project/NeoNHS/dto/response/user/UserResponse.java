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
    private Boolean isBanned;
    private LocalDateTime createdAt;
}