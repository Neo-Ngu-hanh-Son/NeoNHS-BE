package fpt.project.NeoNHS.repository.mongo;

import fpt.project.NeoNHS.document.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * Find all messages in a chat room, with pagination.
     * Sorting should be specified in the Pageable (typically timestamp desc).
     */
    Page<ChatMessage> findByChatRoomId(String chatRoomId, Pageable pageable);

    /**
     * Find messages in a chat room ordered by timestamp descending (for AI
     * conversation history).
     */
    Page<ChatMessage> findByChatRoomIdOrderByTimestampDesc(String chatRoomId, Pageable pageable);
}
