package com.periferia.social.feed.application;

import com.periferia.social.feed.domain.Post;
import com.periferia.social.feed.domain.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ListFeed {

    /** Una publicación junto con el estado del like para quien la está viendo. */
    public record FeedEntry(Post post, boolean likedByMe, boolean own) {}

    public record Result(List<FeedEntry> entries, long totalElements) {}

    private final PostRepository repository;

    public ListFeed(PostRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Result execute(UUID viewerId, int page, int size) {
        List<Post> posts = repository.findFeed(page, size);

        // Una sola consulta resuelve likedByMe de toda la página. Sin esto habría
        // que preguntar por cada publicación por separado: el N+1 clásico.
        Set<UUID> liked = Set.copyOf(
            repository.findLikedPostIds(viewerId, posts.stream().map(Post::id).toList()));

        List<FeedEntry> entries = posts.stream()
            .map(post -> new FeedEntry(post,
                                       liked.contains(post.id()),
                                       post.authorId().equals(viewerId)))
            .toList();

        return new Result(entries, repository.countAll());
    }
}
