package com.periferia.social.feed.infrastructure;

import com.periferia.social.feed.api.LikeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Difunde los contadores de likes a todos los clientes conectados.
 *
 * Sin STOMP ni SockJS: el payload es un único tipo de evento, y el WebSocket
 * nativo funciona igual en navegador y en React Native. SockJS además da problemas
 * en React Native, donde no hay los transportes de respaldo que asume.
 */
@Component
public class LikeWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LikeWebSocketHandler.class);

    /** Concurrente: las conexiones se abren y cierran desde hilos distintos al que difunde. */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * Jackson 3 (`tools.jackson`), que es el que autoconfigura Spring Boot 4.
     * El `com.fasterxml.jackson.databind.ObjectMapper` de siempre sigue en el
     * classpath por dependencias transitivas, pero ya no hay bean de ese tipo.
     */
    private final ObjectMapper objectMapper;

    public LikeWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("Sesión WebSocket abierta: {} (activas: {})", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("Sesión WebSocket cerrada: {} (activas: {})", session.getId(), sessions.size());
    }

    public void broadcast(UUID postId, int likeCount) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(LikeEvent.updated(postId, likeCount));
        } catch (Exception e) {
            log.error("No se pudo serializar el evento de like", e);
            return;
        }

        TextMessage message = new TextMessage(payload);

        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (Exception e) {
                // Una sesión rota no puede impedir que el resto reciba el evento.
                log.warn("No se pudo enviar a la sesión {}: {}", session.getId(), e.getMessage());
                sessions.remove(session.getId());
            }
        });
    }

    /** Expuesto como métrica: sesiones WebSocket activas. */
    public int activeSessions() {
        return sessions.size();
    }
}
