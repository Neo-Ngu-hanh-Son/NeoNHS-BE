package fpt.project.NeoNHS.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

/**
 * Service interface for AI-powered chatbot interactions.
 * Integrates with Google Gemini API for natural language processing,
 * with Function Calling for real-time data queries and RAG for knowledge-based
 * answers.
 */
public interface AiChatService {

    /**
     * Process a user's message in the context of a chat room and stream the AI
     * response
     * back to the client via Server-Sent Events (SSE).
     *
     * @param roomId   the chat room ID (MongoDB)
     * @param senderId the user's UUID (from MySQL)
     * @param message  the user's text message
     * @return an SseEmitter that streams the AI response token-by-token
     */
    SseEmitter streamAiReply(String roomId, String senderId, String message);

    List<Double> getEmbedding(String text);
}
