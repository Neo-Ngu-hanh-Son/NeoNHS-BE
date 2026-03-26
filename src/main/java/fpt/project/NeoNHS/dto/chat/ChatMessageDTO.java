package fpt.project.NeoNHS.dto.chat;

import fpt.project.NeoNHS.enums.MessageStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private String id;
    private String chatRoomId;
    private String senderId;
    private String content;
    private LocalDateTime timestamp;
    private MessageStatus status;
}
