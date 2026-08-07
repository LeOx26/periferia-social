package com.periferia.social.feed.infrastructure;

import com.periferia.social.feed.domain.Post;
import com.periferia.social.feed.domain.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Traduce el puerto de dominio a Spring Data, sin filtrar tipos de JPA hacia arriba. */
@Repository
public class PostRepositoryAdapter implements PostRepository {

    private final JpaPostRepository jpa;

    public PostRepositoryAdapter(JpaPostRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Post save(Post post) {
        return jpa.save(post);
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Post> findFeed(int page, int size) {
        return jpa.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).getContent();
    }

    @Override
    public long countAll() {
        return jpa.count();
    }

    @Override
    public List<UUID> findLikedPostIds(UUID userId, List<UUID> postIds) {
        // Una consulta `IN ()` con lista vacía es SQL inválido en algunos motores.
        return postIds.isEmpty() ? List.of() : jpa.findLikedPostIds(userId, postIds);
    }
}
