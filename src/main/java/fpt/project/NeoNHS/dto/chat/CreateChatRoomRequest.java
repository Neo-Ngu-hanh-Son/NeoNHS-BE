package fpt.project.NeoNHS.dto.chat;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChatRoomRequest {
    private String name; // Optional
    private List<String> participantIds; // User UUID strings
}
