package com.periferia.social.feed.infrastructure;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LikeWebSocketHandlerTest {

    private WebSocketSession openSession(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    @Test
    void broadcasts_the_event_to_every_connected_session() throws Exception {
        var handler = new LikeWebSocketHandler(new ObjectMapper());
        var first = openSession("a");
        var second = openSession("b");
        handler.afterConnectionEstablished(first);
        handler.afterConnectionEstablished(second);

        UUID postId = UUID.randomUUID();
        handler.broadcast(postId, 7);

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(first).sendMessage(captor.capture());
        verify(second).sendMessage(any(TextMessage.class));

        String payload = captor.getValue().getPayload();
        assertTrue(payload.contains("LIKE_UPDATED"));
        assertTrue(payload.contains(postId.toString()));
        assertTrue(payload.contains("\"likeCount\":7"));
    }

    @Test
    void stops_broadcasting_to_a_closed_session() throws Exception {
        var handler = new LikeWebSocketHandler(new ObjectMapper());
        var session = openSession("a");
        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        handler.broadcast(UUID.randomUUID(), 1);

        verify(session, never()).sendMessage(any());
        assertEquals(0, handler.activeSessions());
    }

    /**
     * El fallo que rompe demos: si una sesión muerta lanza a mitad del recorrido,
     * el resto de clientes se queda sin recibir el evento.
     */
    @Test
    void keeps_serving_the_remaining_sessions_when_one_fails_to_receive() throws Exception {
        var handler = new LikeWebSocketHandler(new ObjectMapper());
        var broken = openSession("roto");
        var healthy = openSession("sano");
        doThrow(new IOException("socket roto")).when(broken).sendMessage(any());
        handler.afterConnectionEstablished(broken);
        handler.afterConnectionEstablished(healthy);

        assertDoesNotThrow(() -> handler.broadcast(UUID.randomUUID(), 3));

        verify(healthy).sendMessage(any(TextMessage.class));
        assertEquals(1, handler.activeSessions(), "la sesión rota debe descartarse");
    }
}
