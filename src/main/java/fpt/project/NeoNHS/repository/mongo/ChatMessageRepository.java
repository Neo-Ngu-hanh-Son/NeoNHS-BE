package fpt.project.NeoNHS.repository.mongo;

import fpt.project.NeoNHS.document.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * Find all messages between two users (in either direction), ordered by
     * timestamp descending.
     * This covers both directions: (A→B) and (B→A).
     */
    @Query("{ '$or': [ " +
            "  { 'senderId': ?0, 'receiverId': ?1 }, " +
            "  { 'senderId': ?1, 'receiverId': ?0 } " +
            "] }")
    Page<ChatMessage> findMessagesBetweenUsers(String userId1, String userId2, Pageable pageable);
}
