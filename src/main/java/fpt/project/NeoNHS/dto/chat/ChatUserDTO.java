package fpt.project.NeoNHS.dto.chat;

import fpt.project.NeoNHS.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserDTO {
    private String id;
    private String fullname;
    private String avatarUrl;
    private UserRole role;
}