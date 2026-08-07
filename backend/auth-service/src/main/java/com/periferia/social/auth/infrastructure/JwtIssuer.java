package com.periferia.social.auth.infrastructure;

import com.periferia.social.auth.domain.User;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Emite los JWT que social-service verificará en local. El alias viaja como claim
 * precisamente para que social-service pueda sellar la autoría de una publicación
 * sin consultar nunca esta base de datos.
 */
@Component
public class JwtIssuer {

    private final SecretKey key;
    private final Duration expiration;

    public JwtIssuer(@Value("${security.jwt.secret}") String secret,
                     @Value("${security.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String issue(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.id().toString())
            .claim("alias", user.alias())
            .claim("username", user.username())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expiration)))
            .signWith(key)
            .compact();
    }

    public long expiresInSeconds() {
        return expiration.toSeconds();
    }
}
