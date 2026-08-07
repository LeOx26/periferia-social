package com.periferia.social.feed.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Verifica la firma del JWT en memoria con el secreto compartido. Nunca llama a
 * auth-service: si la firma cuadra, el contenido del token es auténtico, y si no
 * cuadra se rechaza. Esto elimina un salto de red por petición y hace que el feed
 * siga funcionando aunque auth-service esté caído.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey key;

    public JwtAuthenticationFilter(@Value("${security.jwt.secret}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(header.substring(7))
                    .getPayload();

                var principal = new AuthenticatedUser(
                    UUID.fromString(claims.getSubject()),
                    claims.get("alias", String.class));

                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, List.of()));
            } catch (JwtException | IllegalArgumentException e) {
                // Token inválido, expirado o manipulado: se continúa sin autenticar y
                // la cadena de seguridad responderá 401 en las rutas protegidas.
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
