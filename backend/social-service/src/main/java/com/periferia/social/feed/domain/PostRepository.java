package com.periferia.social.feed.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia definido por el dominio. El adaptador JPA vive en
 * infrastructure, así que el dominio no depende de Spring Data.
 */
public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(UUID id);

    /** Feed ordenado por fecha descendente. No materializa la colección de likes. */
    List<Post> findFeed(int page, int size);

    long countAll();

    /**
     * De las publicaciones dadas, cuáles ha likeado ya el usuario. Una sola consulta
     * para toda la página: preguntarlo publicación por publicación sería un N+1.
     */
    List<UUID> findLikedPostIds(UUID userId, List<UUID> postIds);
}
