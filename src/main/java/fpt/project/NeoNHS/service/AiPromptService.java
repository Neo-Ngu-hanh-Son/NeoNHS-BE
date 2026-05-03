package fpt.project.NeoNHS.service;

import com.fasterxml.jackson.databind.node.ArrayNode;

public interface AiPromptService {
    String getSystemPrompt(String userMessage);
    ArrayNode buildConversationHistory(String roomId, String userMessage);
    String searchKnowledgeBaseContext(String keyword);
}
