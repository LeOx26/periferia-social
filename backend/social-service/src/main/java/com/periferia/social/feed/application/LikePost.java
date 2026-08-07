package com.periferia.social.feed.application;

import com.periferia.social.feed.domain.Post;
import com.periferia.social.feed.domain.PostNotFoundException;
import com.periferia.social.feed.domain.PostRepository;
import com.periferia.social.feed.infrastructure.FeedMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class LikePost {

    private final PostRepository repository;
    private final Clock clock;
    private final FeedMetrics metrics;

    public LikePost(PostRepository repository, Clock clock, FeedMetrics metrics) {
        this.repository = repository;
        this.clock = clock;
        this.metrics = metrics;
    }

    /**
     * El caso de uso no decide nada: carga el agregado, le pide que registre el
     * like y lo persiste. Las reglas de auto-like y doble like viven en Post.
     */
    @Transactional
    public Post execute(UUID postId, UUID userId) {
        Post post = repository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));

        post.like(userId, clock);
        Post saved = repository.save(post);

        // Después de persistir: un like rechazado por el dominio no cuenta.
        metrics.likeRegistered();

        return saved;
    }
}
