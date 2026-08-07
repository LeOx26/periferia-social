package com.periferia.social.feed.infrastructure;

import com.periferia.social.feed.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaPostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Resuelve `likedByMe` para toda la página con una única consulta. Preguntarlo
     * publicación por publicación produciría un N+1.
     */
    @Query("""
           SELECT l.id.postId FROM PostLike l
           WHERE l.id.userId = :userId AND l.id.postId IN :postIds
           """)
    List<UUID> findLikedPostIds(@Param("userId") UUID userId, @Param("postIds") List<UUID> postIds);
}
