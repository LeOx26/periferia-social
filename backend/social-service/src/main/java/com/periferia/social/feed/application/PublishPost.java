package com.periferia.social.feed.application;

import com.periferia.social.feed.domain.Post;
import com.periferia.social.feed.domain.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class PublishPost {

    private final PostRepository repository;
    private final Clock clock;

    public PublishPost(PostRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** El caso de uso no valida nada: las reglas están en el agregado. */
    @Transactional
    public Post execute(UUID authorId, String authorAlias, String message) {
        return repository.save(Post.publish(authorId, authorAlias, message, clock));
    }
}
