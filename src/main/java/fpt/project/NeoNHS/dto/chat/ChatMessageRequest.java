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
    private String messageType;
    private String mediaUrl;
    private java.util.Map<String, Object> metadata;
}
