package com.periferia.social.feed.infrastructure;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Autentica el handshake del WebSocket.
 *
 * El objeto WebSocket del navegador no permite enviar cabeceras al abrir la
 * conexión, así que el token viaja como parámetro de consulta. Es un trade-off
 * consciente: en producción se emitiría un ticket de un solo uso por HTTP, para
 * que el JWT no acabe en los logs de acceso del servidor ni en el historial.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final SecretKey key;

    public JwtHandshakeInterceptor(@Value("${security.jwt.secret}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler handler, Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUri(request.getURI())
            .build().getQueryParams().getFirst("token");

        if (token == null || token.isBlank()) {
            log.warn("Handshake WebSocket rechazado: falta el token");
            return false;
        }

        try {
            var claims = Jwts.parser().verifyWith(key).build()
                             .parseSignedClaims(token).getPayload();

            attributes.put("userId", claims.getSubject());
            attributes.put("alias", claims.get("alias", String.class));
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Handshake WebSocket rechazado: token inválido");
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler handler, Exception exception) {
        // Sin acciones posteriores al handshake.
    }
}
