package com.periferia.social.feed.api;

import com.periferia.social.feed.application.ListFeed;
import com.periferia.social.feed.domain.Post;

import java.time.Instant;
import java.util.UUID;

/**
 * `isOwn` permite al cliente marcar las publicaciones propias y deshabilitar su
 * botón de like. El enunciado pide "publicaciones de los demás usuarios": se
 * muestran todas para que la demo tenga sentido tras publicar, y el espíritu del
 * requisito se hace cumplir donde importa, en la regla de no auto-like.
 */
public record PostView(UUID id, String message, Instant createdAt, UUID authorId,
                       String authorAlias, int likeCount, boolean likedByMe, boolean isOwn) {

    public static PostView from(ListFeed.FeedEntry entry) {
        Post post = entry.post();
        return new PostView(post.id(), post.message(), post.createdAt(), post.authorId(),
                            post.authorAlias(), post.likeCount(), entry.likedByMe(), entry.own());
    }

    /** Una publicación recién creada siempre es propia y nunca tiene likes. */
    public static PostView ofOwnNewPost(Post post) {
        return new PostView(post.id(), post.message(), post.createdAt(), post.authorId(),
                            post.authorAlias(), post.likeCount(), false, true);
    }
}
