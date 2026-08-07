package com.periferia.social.feed.application;

import com.periferia.social.feed.domain.Post;
import com.periferia.social.feed.domain.PostRepository;
import com.periferia.social.feed.infrastructure.FeedMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class PublishPost {

    private final PostRepository repository;
    private final Clock clock;
    private final FeedMetrics metrics;

    public PublishPost(PostRepository repository, Clock clock, FeedMetrics metrics) {
        this.repository = repository;
        this.clock = clock;
        this.metrics = metrics;
    }

    /** El caso de uso no valida nada: las reglas están en el agregado. */
    @Transactional
    public Post execute(UUID authorId, String authorAlias, String message) {
        Post post = repository.save(Post.publish(authorId, authorAlias, message, clock));

        // Después de persistir: si el dominio rechaza el mensaje, la métrica no se mueve.
        metrics.postCreated();

        return post;
    }
}
