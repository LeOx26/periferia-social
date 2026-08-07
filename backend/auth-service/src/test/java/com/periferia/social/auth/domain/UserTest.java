package com.periferia.social.auth.domain;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private User userWithPassword(String raw) {
        return new User(
            UUID.randomUUID(), "leo", ENCODER.encode(raw),
            "Leonel", "Benítez", LocalDate.of(1993, 4, 12), "leo"
        );
    }

    @Test
    void matches_the_correct_password() {
        assertTrue(userWithPassword("Periferia2026!").matchesPassword("Periferia2026!", ENCODER));
    }

    @Test
    void rejects_an_incorrect_password() {
        assertFalse(userWithPassword("Periferia2026!").matchesPassword("otra", ENCODER));
    }

    @Test
    void never_exposes_the_password_hash_in_toString() {
        String rendered = userWithPassword("Periferia2026!").toString();
        assertFalse(rendered.contains("$2a$"), "toString no debe filtrar el hash");
    }
}
