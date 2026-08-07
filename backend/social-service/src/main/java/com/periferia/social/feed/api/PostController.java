package com.periferia.social.feed.api;

import com.periferia.social.feed.application.ListFeed;
import com.periferia.social.feed.application.PublishPost;
import com.periferia.social.feed.infrastructure.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Publicaciones", description = "Feed y creación de publicaciones")
public class PostController {

    private final ListFeed listFeed;
    private final PublishPost publishPost;
    private final int maxPageSize;

    public PostController(ListFeed listFeed, PublishPost publishPost,
                          @Value("${feed.max-page-size}") int maxPageSize) {
        this.listFeed = listFeed;
        this.publishPost = publishPost;
        this.maxPageSize = maxPageSize;
    }

    @GetMapping
    @Operation(summary = "Lista el feed. Las publicaciones propias vienen marcadas con isOwn.")
    public FeedPage feed(@AuthenticationPrincipal AuthenticatedUser viewer,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size) {
        // Acotar el tamaño impide que un cliente pida cien mil publicaciones de una vez.
        int safeSize = Math.min(Math.max(size, 1), maxPageSize);
        int safePage = Math.max(page, 0);

        var result = listFeed.execute(viewer.id(), safePage, safeSize);

        return new FeedPage(result.entries().stream().map(PostView::from).toList(),
                            safePage, safeSize, result.totalElements());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una publicación. El autor y la fecha los fija el servidor.")
    public PostView create(@AuthenticationPrincipal AuthenticatedUser author,
                           @Valid @RequestBody CreatePostRequest request) {
        return PostView.ofOwnNewPost(
            publishPost.execute(author.id(), author.alias(), request.message()));
    }
}
