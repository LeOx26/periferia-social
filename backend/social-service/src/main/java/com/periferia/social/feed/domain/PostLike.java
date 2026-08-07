package com.periferia.social.feed.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Parte del agregado Post: no se manipula desde fuera, solo a través de
 * Post.like() y Post.unlike(). Por eso el constructor es de ámbito de paquete.
 */
@Entity
@Table(name = "post_likes")
public class PostLike {

    @EmbeddedId
    private PostLikeId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PostLike() { /* requerido por JPA */ }

    PostLike(Post post, UUID userId, Instant createdAt) {
        this.post = post;
        this.id = new PostLikeId(post.id(), userId);
        this.createdAt = createdAt;
    }

    UUID userId() {
        return id.userId();
    }

    @Embeddable
    public record PostLikeId(
        @Column(name = "post_id") UUID postId,
        @Column(name = "user_id") UUID userId
    ) {}

    @Override
    public boolean equals(Object other) {
        return other instanceof PostLike that && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
