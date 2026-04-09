package fpt.project.NeoNHS.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

/**
 * Channel interceptor that sets the STOMP user Principal from the
 * userId stored in session attributes during the WebSocket handshake.
 * This is required for convertAndSendToUser() to work correctly.
 */
@Component
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

            if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
                String userId = (String) sessionAttributes.get("userId");

                // Set the user Principal so that convertAndSendToUser can route messages
                accessor.setUser(new StompPrincipal(userId));
                log.info("STOMP CONNECT: Set principal for user {}", userId);
            }
        }

        return message;
    }

    /**
     * Simple Principal implementation using the user ID string.
     */
    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
