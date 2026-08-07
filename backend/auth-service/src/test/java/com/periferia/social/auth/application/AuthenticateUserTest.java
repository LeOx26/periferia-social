package com.periferia.social.auth.application;

import com.periferia.social.auth.domain.InvalidCredentialsException;
import com.periferia.social.auth.domain.User;
import com.periferia.social.auth.domain.UserRepository;
import com.periferia.social.auth.infrastructure.JwtIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticateUserTest {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private UserRepository repository;
    private AuthenticateUser authenticate;
    private User leo;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        JwtIssuer issuer = new JwtIssuer("un-secreto-de-desarrollo-de-al-menos-32-bytes!", 60);
        authenticate = new AuthenticateUser(repository, ENCODER, issuer);
        leo = new User(UUID.randomUUID(), "leo", ENCODER.encode("Periferia2026!"),
                       "Leonel", "Benítez", LocalDate.of(1993, 4, 12), "leo");
    }

    @Test
    void returns_a_token_for_valid_credentials() {
        when(repository.findByUsername("leo")).thenReturn(Optional.of(leo));

        var result = authenticate.execute("leo", "Periferia2026!");

        assertNotNull(result.token());
        assertEquals(leo.id(), result.user().id());
        assertTrue(result.expiresInSeconds() > 0);
    }

    @Test
    void rejects_a_wrong_password() {
        when(repository.findByUsername("leo")).thenReturn(Optional.of(leo));

        assertThrows(InvalidCredentialsException.class,
                     () -> authenticate.execute("leo", "incorrecta"));
    }

    /**
     * Usuario inexistente y contraseña incorrecta devuelven el mismo mensaje:
     * de lo contrario la API permitiría enumerar qué usuarios existen.
     */
    @Test
    void rejects_an_unknown_user_without_revealing_that_it_does_not_exist() {
        when(repository.findByUsername("fantasma")).thenReturn(Optional.empty());

        var thrown = assertThrows(InvalidCredentialsException.class,
                                  () -> authenticate.execute("fantasma", "loquesea"));
        assertEquals("Usuario o contraseña incorrectos", thrown.getMessage());
    }
}
