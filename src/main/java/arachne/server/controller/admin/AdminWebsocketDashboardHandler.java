package arachne.server.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Slf4j
@Component
public class AdminWebsocketDashboardHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    private final ConcurrentMap<String, WebSocketSession> subscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.debug("Session {} connected.", session.getId());
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
            this.subscriptions.put(session.getId(), session);
            log.debug("Add to subscription list.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.debug("Session {} disconnect.", session.getId());
        this.subscriptions.remove(session.getId());
    }

    public void broadcast(final Supplier<Object> messageSupplier) {
        if (null != messageSupplier && !this.subscriptions.isEmpty()) {
            this.subscriptions.forEach((id, session) -> {
                try {
                    session.sendMessage(new TextMessage(this.mapper.writeValueAsBytes(messageSupplier.get())));
                } catch (Exception ex) {
                    log.error("Illegal message payload", ex);
                }
            });
        }
    }

}
