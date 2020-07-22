package arachne.server.controller;

import arachne.server.controller.admin.AdminWebsocketDashboardHandler;
import arachne.server.controller.worker.WebsocketProtoHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AdminWebsocketDashboardHandler adminDashboardHandler;

    @Autowired
    private WebsocketProtoHandler workerProtoHandler;

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(adminDashboardHandler, "/ws/admin/dashboard/");
        registry.addHandler(workerProtoHandler, "/ws/worker/proto/feedback/");
    }
}
