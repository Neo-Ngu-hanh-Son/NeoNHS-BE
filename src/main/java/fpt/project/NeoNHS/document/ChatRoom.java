package fpt.project.NeoNHS.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    private String id;

    private String name; // Optional room name (null for 1-on-1 chats)

    @Indexed
    private List<String> participants; // List of user UUID strings from MySQL

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastMessageAt;

    private String lastMessagePreview; // Truncated content of the last message

    private String lastMessageSenderId; // Who sent the last message
}
