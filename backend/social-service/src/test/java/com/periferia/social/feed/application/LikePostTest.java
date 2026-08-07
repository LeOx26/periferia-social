package com.periferia.social.feed.application;

import com.periferia.social.feed.domain.DuplicateLikeException;
import com.periferia.social.feed.domain.Post;
import com.periferia.social.feed.domain.PostNotFoundException;
import com.periferia.social.feed.domain.PostRepository;
import com.periferia.social.feed.domain.SelfLikeNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LikePostTest {

    private static final UUID AUTHOR = UUID.randomUUID();
    private static final UUID READER = UUID.randomUUID();
    private static final Clock CLOCK =
        Clock.fixed(Instant.parse("2026-08-07T10:00:00Z"), ZoneOffset.UTC);

    private PostRepository repository;
    private LikePost like;
    private UnlikePost unlike;
    private Post post;

    @BeforeEach
    void setUp() {
        repository = mock(PostRepository.class);
        like = new LikePost(repository, CLOCK);
        unlike = new UnlikePost(repository);

        post = Post.publish(AUTHOR, "leo", "Hola", CLOCK);
        when(repository.findById(post.id())).thenReturn(Optional.of(post));
        when(repository.save(any(Post.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void records_a_like_and_returns_the_updated_post() {
        Post updated = like.execute(post.id(), READER);
        assertEquals(1, updated.likeCount());
    }

    /** El caso de uso no reimplementa la regla: la propaga desde el agregado. */
    @Test
    void propagates_the_self_like_rule_from_the_domain() {
        assertThrows(SelfLikeNotAllowedException.class, () -> like.execute(post.id(), AUTHOR));
    }

    @Test
    void propagates_the_duplicate_like_rule_from_the_domain() {
        like.execute(post.id(), READER);
        assertThrows(DuplicateLikeException.class, () -> like.execute(post.id(), READER));
    }

    @Test
    void fails_with_a_domain_error_when_the_post_does_not_exist() {
        UUID unknown = UUID.randomUUID();
        when(repository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> like.execute(unknown, READER));
    }

    @Test
    void withdraws_a_like() {
        like.execute(post.id(), READER);

        Post updated = unlike.execute(post.id(), READER);

        assertEquals(0, updated.likeCount());
    }
}
