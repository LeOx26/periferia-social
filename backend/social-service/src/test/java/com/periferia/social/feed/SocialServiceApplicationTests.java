package com.periferia.social.feed;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Levanta el contexto completo contra un Postgres efímero, de modo que
 * `mvnw test` funciona en un CI limpio sin el docker-compose levantado.
 */
@SpringBootTest
@Testcontainers
class SocialServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("socialdb");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired DataSource dataSource;

    @Test
    void applies_the_flyway_migrations_and_seeds_one_post_per_user() throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM posts")) {
            rows.next();
            assertEquals(5, rows.getInt(1));
        }
    }

    @Test
    void enforces_the_composite_key_that_makes_a_duplicate_like_impossible() throws Exception {
        String postId = "aaaaaaaa-0001-4000-8000-000000000001";
        String userId = "22222222-2222-4222-8222-222222222222";

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {

            statement.execute(
                "INSERT INTO post_likes (post_id, user_id) VALUES ('%s', '%s')"
                    .formatted(postId, userId));

            // El motor debe rechazar el segundo insert aunque el dominio no intervenga.
            var duplicate = org.junit.jupiter.api.Assertions.assertThrows(
                java.sql.SQLException.class,
                () -> statement.execute(
                    "INSERT INTO post_likes (post_id, user_id) VALUES ('%s', '%s')"
                        .formatted(postId, userId)));

            org.junit.jupiter.api.Assertions.assertTrue(
                duplicate.getMessage().toLowerCase().contains("duplicate")
                    || duplicate.getMessage().toLowerCase().contains("llave duplicada"),
                "se esperaba una violación de clave primaria, pero fue: " + duplicate.getMessage());
        }
    }
}
