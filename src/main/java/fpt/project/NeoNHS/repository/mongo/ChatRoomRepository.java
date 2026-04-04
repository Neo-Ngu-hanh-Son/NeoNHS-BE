package fpt.project.NeoNHS.repository.mongo;

import fpt.project.NeoNHS.document.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    /**
     * Find all chat rooms that a user is a participant in,
     * ordered by last message time (most recent first).
     */
    List<ChatRoom> findByParticipantsContainingOrderByLastMessageAtDesc(String userId);

    /**
     * Find a chat room that contains ALL the specified participants and no others.
     * Useful to avoid creating duplicate 1-on-1 rooms.
     */
    @Query("{ 'participants': { $all: ?0, $size: ?1 } }")
    Optional<ChatRoom> findByExactParticipants(List<String> participants, int size);
}
