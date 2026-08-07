package com.periferia.social.feed.application;

import com.periferia.social.feed.domain.InvalidPostMessageException;
import com.periferia.social.feed.domain.Post;
import com.periferia.social.feed.domain.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublishPostTest {

    private static final UUID AUTHOR = UUID.randomUUID();
    private static final Instant FIXED = Instant.parse("2026-08-07T10:00:00Z");

    private PostRepository repository;
    private PublishPost publish;

    @BeforeEach
    void setUp() {
        repository = mock(PostRepository.class);
        when(repository.save(any(Post.class))).thenAnswer(call -> call.getArgument(0));
        publish = new PublishPost(repository, Clock.fixed(FIXED, ZoneOffset.UTC));
    }

    @Test
    void persists_a_post_authored_by_the_authenticated_user() {
        Post post = publish.execute(AUTHOR, "leo", "Mi primera publicación");

        assertEquals(AUTHOR, post.authorId());
        assertEquals("leo", post.authorAlias());
        assertEquals(FIXED, post.createdAt());
        verify(repository).save(post);
    }

    @Test
    void does_not_persist_anything_when_the_message_is_invalid() {
        assertThrows(InvalidPostMessageException.class, () -> publish.execute(AUTHOR, "leo", "  "));
        verify(repository, never()).save(any());
    }
}
