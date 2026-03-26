package fpt.project.NeoNHS.dto.chat;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {
    private String chatRoomId;
    private String content;
}
