package com.periferia.social.feed;

import com.periferia.social.feed.application.LikePost;
import com.periferia.social.feed.application.ListFeed;
import com.periferia.social.feed.application.PublishPost;
import com.periferia.social.feed.application.UnlikePost;
import com.periferia.social.feed.domain.DuplicateLikeException;
import com.periferia.social.feed.domain.Post;
import com.periferia.social.feed.domain.SelfLikeNotAllowedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica el recorrido completo contra un Postgres real: es lo único que ningún
 * mock puede demostrar, porque comprueba que las reglas del dominio sobreviven al
 * viaje de ida y vuelta a la base de datos.
 */
@SpringBootTest
@Testcontainers
class FeedIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("socialdb");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired PublishPost publishPost;
    @Autowired LikePost likePost;
    @Autowired UnlikePost unlikePost;
    @Autowired ListFeed listFeed;

    @Test
    void persists_a_post_and_registers_a_like_from_another_user() {
        UUID author = UUID.randomUUID();
        UUID reader = UUID.randomUUID();

        Post created = publishPost.execute(author, "leo", "Publicación de integración");
        Post liked = likePost.execute(created.id(), reader);

        assertEquals(1, liked.likeCount());
    }

    @Test
    void rejects_a_second_like_from_the_same_user_against_a_real_database() {
        UUID author = UUID.randomUUID();
        UUID reader = UUID.randomUUID();

        Post created = publishPost.execute(author, "leo", "Otra publicación");
        likePost.execute(created.id(), reader);

        assertThrows(DuplicateLikeException.class, () -> likePost.execute(created.id(), reader));
    }

    @Test
    void rejects_a_like_from_the_author_against_a_real_database() {
        UUID author = UUID.randomUUID();

        Post created = publishPost.execute(author, "leo", "No deberia poder likear esto");

        assertThrows(SelfLikeNotAllowedException.class, () -> likePost.execute(created.id(), author));
    }

    @Test
    void round_trips_the_like_state_through_the_feed_projection() {
        UUID author = UUID.randomUUID();
        UUID reader = UUID.randomUUID();

        Post created = publishPost.execute(author, "leo", "Publicación observada desde el feed");
        likePost.execute(created.id(), reader);

        // El feed lee likedByMe con la consulta agrupada, no recargando el agregado.
        var visto = listFeed.execute(reader, 0, 50).entries().stream()
            .filter(entry -> entry.post().id().equals(created.id()))
            .findFirst()
            .orElseThrow();

        assertTrue(visto.likedByMe(), "el lector ya dio like");
        assertFalse(visto.own(), "no es su publicación");
        assertEquals(1, visto.post().likeCount());
    }

    @Test
    void withdrawing_a_like_persists_and_allows_liking_again() {
        UUID author = UUID.randomUUID();
        UUID reader = UUID.randomUUID();

        Post created = publishPost.execute(author, "leo", "Like, unlike y like otra vez");
        likePost.execute(created.id(), reader);
        unlikePost.execute(created.id(), reader);

        // Si el unlike no borrase la fila, este like fallaría con clave duplicada.
        Post reliked = likePost.execute(created.id(), reader);

        assertEquals(1, reliked.likeCount());
    }
}
