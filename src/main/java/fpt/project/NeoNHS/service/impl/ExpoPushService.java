package fpt.project.NeoNHS.service.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpoPushService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    public void sendPushNotification(List<String> expoPushTokens, String title, String body, Map<String, Object> data) {
        if (expoPushTokens == null || expoPushTokens.isEmpty())
            return;

        List<Map<String, Object>> messages = new ArrayList<>();
        for (String token : expoPushTokens) {
            if (token != null && token.startsWith("ExponentPushToken")) {
                Map<String, Object> message = new HashMap<>();
                message.put("to", token);
                message.put("title", title);
                message.put("body", body);
                message.put("data", data);
                message.put("sound", "default");
                messages.add(message);
            }
        }

        if (!messages.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(messages, headers);
            try {
                restTemplate.postForObject(EXPO_PUSH_URL, request, String.class);
            } catch (Exception e) {
                System.err.println("Failed to send Expo push: " + e.getMessage());
            }
        }
    }
}
