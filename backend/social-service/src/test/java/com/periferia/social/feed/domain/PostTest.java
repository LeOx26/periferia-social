package com.periferia.social.feed.domain;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Especificación ejecutable de las reglas de negocio del feed.
 *
 * Estas reglas viven en el agregado y no en procedimientos almacenados: por eso
 * este fichero no necesita Spring, ni base de datos, ni contenedores, y corre en
 * milisegundos.
 */
class PostTest {

    private static final UUID AUTHOR = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID OTHER  = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final Instant FIXED = Instant.parse("2026-08-07T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(FIXED, ZoneOffset.UTC);

    private Post aPost() {
        return Post.publish(AUTHOR, "leo", "Hola mundo", CLOCK);
    }

    // --- Publicación ---

    @Test
    void stamps_the_creation_time_from_the_injected_clock() {
        assertEquals(FIXED, aPost().createdAt());
    }

    @Test
    void trims_surrounding_whitespace_from_the_message() {
        assertEquals("Hola mundo", Post.publish(AUTHOR, "leo", "   Hola mundo  ", CLOCK).message());
    }

    @Test
    void rejects_a_blank_message() {
        var thrown = assertThrows(InvalidPostMessageException.class,
                                  () -> Post.publish(AUTHOR, "leo", "   ", CLOCK));
        assertEquals("El mensaje no puede estar vacío", thrown.getMessage());
    }

    @Test
    void rejects_a_message_longer_than_the_maximum() {
        String tooLong = "x".repeat(Post.MAX_MESSAGE_LENGTH + 1);
        assertThrows(InvalidPostMessageException.class,
                     () -> Post.publish(AUTHOR, "leo", tooLong, CLOCK));
    }

    @Test
    void accepts_a_message_of_exactly_the_maximum_length() {
        String exact = "x".repeat(Post.MAX_MESSAGE_LENGTH);
        assertEquals(exact, Post.publish(AUTHOR, "leo", exact, CLOCK).message());
    }

    @Test
    void starts_with_zero_likes() {
        assertEquals(0, aPost().likeCount());
    }

    // --- Likes ---

    @Test
    void increments_the_counter_when_another_user_likes_it() {
        Post post = aPost();
        post.like(OTHER, CLOCK);
        assertEquals(1, post.likeCount());
        assertTrue(post.isLikedBy(OTHER));
    }

    @Test
    void rejects_a_like_from_the_author() {
        Post post = aPost();
        var thrown = assertThrows(SelfLikeNotAllowedException.class, () -> post.like(AUTHOR, CLOCK));
        assertEquals("No puedes dar like a tu propia publicación", thrown.getMessage());
        assertEquals(0, post.likeCount());
    }

    @Test
    void rejects_a_duplicate_like_from_the_same_user() {
        Post post = aPost();
        post.like(OTHER, CLOCK);
        assertThrows(DuplicateLikeException.class, () -> post.like(OTHER, CLOCK));
        assertEquals(1, post.likeCount(), "el contador no debe moverse tras un like rechazado");
    }

    @Test
    void decrements_the_counter_when_the_like_is_withdrawn() {
        Post post = aPost();
        post.like(OTHER, CLOCK);
        post.unlike(OTHER);
        assertEquals(0, post.likeCount());
        assertFalse(post.isLikedBy(OTHER));
    }

    @Test
    void treats_unlike_without_a_previous_like_as_a_no_op() {
        Post post = aPost();
        assertDoesNotThrow(() -> post.unlike(OTHER));
        assertEquals(0, post.likeCount());
    }
}
