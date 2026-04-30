package fpt.project.NeoNHS.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public interface AiFunctionCallingService {
    JsonNode buildToolDeclarations();
    String executeFunctionCall(String functionName, JsonNode args, String senderId, Map<String, Object> metadata);
}
