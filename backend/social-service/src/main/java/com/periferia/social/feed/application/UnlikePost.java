package com.periferia.social.feed.application;

import com.periferia.social.feed.domain.Post;
import com.periferia.social.feed.domain.PostNotFoundException;
import com.periferia.social.feed.domain.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UnlikePost {

    private final PostRepository repository;

    public UnlikePost(PostRepository repository) {
        this.repository = repository;
    }

    /** Idempotente por diseño: retirar un like inexistente devuelve el post sin cambios. */
    @Transactional
    public Post execute(UUID postId, UUID userId) {
        Post post = repository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));

        post.unlike(userId);

        return repository.save(post);
    }
}
