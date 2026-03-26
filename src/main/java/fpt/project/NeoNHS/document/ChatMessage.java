package fpt.project.NeoNHS.document;

import fpt.project.NeoNHS.enums.MessageStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@CompoundIndex(name = "chat_participants_idx", def = "{'senderId': 1, 'receiverId': 1, 'timestamp': -1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    private String id;

    private String senderId; // UUID string from MySQL User table
    private String receiverId; // UUID string from MySQL User table

    private String content;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    // Future extension fields (not used yet):
    // private String messageType; // TEXT, IMAGE, FILE, etc.
    // private String mediaUrl; // URL for image/file attachments
    // private String aiResponse; // AI chatbot response metadata
}
