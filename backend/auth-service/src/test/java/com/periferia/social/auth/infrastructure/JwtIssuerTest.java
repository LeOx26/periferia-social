package com.periferia.social.auth.infrastructure;

import com.periferia.social.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtIssuerTest {

    private static final String SECRET = "un-secreto-de-desarrollo-de-al-menos-32-bytes!";

    private User anyUser(UUID id) {
        return new User(id, "leo", "hash", "Leonel", "Benítez",
                        LocalDate.of(1993, 4, 12), "leo");
    }

    @Test
    void issues_a_token_carrying_the_user_id_and_alias() {
        UUID id = UUID.randomUUID();

        String token = new JwtIssuer(SECRET, 60).issue(anyUser(id));

        Claims claims = Jwts.parser()
            .verifyWith(new SecretKeySpec(SECRET.getBytes(), "HmacSHA256"))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertEquals(id.toString(), claims.getSubject());
        assertEquals("leo", claims.get("alias", String.class));
        assertTrue(claims.getExpiration().after(new java.util.Date()));
    }

    /**
     * Este test es la demostración viva de la decisión D3: social-service puede
     * verificar el token sin llamar a auth-service porque, sin el secreto correcto,
     * la firma no cuadra y el token se rechaza.
     */
    @Test
    void produces_a_token_that_fails_verification_under_a_different_secret() {
        String token = new JwtIssuer(SECRET, 60).issue(anyUser(UUID.randomUUID()));

        assertThrows(io.jsonwebtoken.security.SignatureException.class, () ->
            Jwts.parser()
                .verifyWith(new SecretKeySpec("otro-secreto-completamente-distinto-32!".getBytes(), "HmacSHA256"))
                .build()
                .parseSignedClaims(token));
    }
}
