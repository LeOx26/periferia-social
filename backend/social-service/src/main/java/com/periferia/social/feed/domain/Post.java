package com.periferia.social.feed.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Raíz de agregado del feed.
 *
 * Todas las reglas de negocio viven aquí y no en la base de datos: es la
 * alternativa deliberada a resolver los likes con procedimientos almacenados en
 * PL/pgSQL. A cambio, las reglas se testean sin infraestructura, quedan bajo
 * control de versiones y se leen en un único sitio.
 *
 * La restricción UNIQUE(post_id, user_id) del esquema es la red de seguridad ante
 * concurrencia, no la sede de la regla.
 */
@Entity
@Table(name = "posts")
public class Post {

    public static final int MAX_MESSAGE_LENGTH = 280;

    @Id
    private UUID id;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    /** Denormalizado desde el claim del JWT: el feed no consulta a auth-service. */
    @Column(name = "author_alias", nullable = false)
    private String authorAlias;

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    /**
     * Carga perezosa a propósito: el feed nunca toca esta colección, porque usa el
     * contador denormalizado y resuelve `likedByMe` con una sola consulta para toda
     * la página. Solo se materializa al dar o quitar un like.
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.LAZY)
    private Set<PostLike> likes = new HashSet<>();

    protected Post() { /* requerido por JPA */ }

    private Post(UUID id, UUID authorId, String authorAlias, String message, Instant createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.authorAlias = authorAlias;
        this.message = message;
        this.createdAt = createdAt;
        this.likeCount = 0;
    }

    /** Única forma de crear una publicación: no hay constructor público que se salte las reglas. */
    public static Post publish(UUID authorId, String authorAlias, String rawMessage, Clock clock) {
        String message = rawMessage == null ? "" : rawMessage.trim();

        if (message.isEmpty()) {
            throw new InvalidPostMessageException("El mensaje no puede estar vacío");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidPostMessageException(
                "El mensaje no puede superar los " + MAX_MESSAGE_LENGTH + " caracteres");
        }

        return new Post(UUID.randomUUID(), authorId, authorAlias, message, clock.instant());
    }

    public void like(UUID userId, Clock clock) {
        if (authorId.equals(userId)) {
            throw new SelfLikeNotAllowedException();
        }
        if (isLikedBy(userId)) {
            throw new DuplicateLikeException();
        }

        likes.add(new PostLike(this, userId, clock.instant()));
        likeCount++;
    }

    /** Idempotente: retirar un like que no existe no es un error, es un no-op. */
    public void unlike(UUID userId) {
        if (likes.removeIf(like -> like.userId().equals(userId))) {
            likeCount = Math.max(0, likeCount - 1);
        }
    }

    public boolean isLikedBy(UUID userId) {
        return likes.stream().anyMatch(like -> like.userId().equals(userId));
    }

    public UUID id() { return id; }
    public UUID authorId() { return authorId; }
    public String authorAlias() { return authorAlias; }
    public String message() { return message; }
    public Instant createdAt() { return createdAt; }
    public int likeCount() { return likeCount; }
}
