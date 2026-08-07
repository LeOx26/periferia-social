package com.periferia.social.auth;

import com.periferia.social.auth.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Levanta el contexto completo contra un Postgres efímero. Usar Testcontainers en
 * vez de la base del docker-compose mantiene `mvn test` hermético: funciona en un
 * CI limpio sin nada previamente levantado.
 */
@SpringBootTest
@Testcontainers
class AuthServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("authdb");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired UserRepository userRepository;

    @Test
    void applies_the_flyway_migrations_and_seeds_the_demo_users() {
        assertTrue(userRepository.findByUsername("leo").isPresent());
        assertEquals("Leonel", userRepository.findByUsername("leo").orElseThrow().firstName());
    }

    @Test
    void does_not_find_a_user_that_was_never_seeded() {
        assertTrue(userRepository.findByUsername("fantasma").isEmpty());
    }
}
