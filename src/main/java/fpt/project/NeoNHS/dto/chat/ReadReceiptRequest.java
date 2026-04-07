package fpt.project.NeoNHS.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptRequest {
    private String chatRoomId;
    private String lastReadMessageId;
}
