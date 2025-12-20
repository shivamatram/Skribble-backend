package com.skribble.config;

import com.skribble.websocket.GameWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * WebSocket configuration for Skribble game server.
 * Configures the WebSocket endpoint and handler.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;

    @Value("${websocket.endpoint:/ws/game}")
    private String websocketEndpoint;

    @Value("${websocket.allowed-origins:*}")
    private String[] allowedOrigins;

    public WebSocketConfig(GameWebSocketHandler gameWebSocketHandler) {
        this.gameWebSocketHandler = gameWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler, websocketEndpoint)
                .setAllowedOrigins(allowedOrigins);
    }

    /**
     * Configure WebSocket container settings.
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer(
            @Value("${websocket.max-session-idle-timeout:300000}") long maxSessionIdleTimeout,
            @Value("${websocket.max-text-message-buffer-size:65536}") int maxTextMessageBufferSize,
            @Value("${websocket.max-binary-message-buffer-size:65536}") int maxBinaryMessageBufferSize) {
        
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxSessionIdleTimeout(maxSessionIdleTimeout);
        container.setMaxTextMessageBufferSize(maxTextMessageBufferSize);
        container.setMaxBinaryMessageBufferSize(maxBinaryMessageBufferSize);
        return container;
    }
}
