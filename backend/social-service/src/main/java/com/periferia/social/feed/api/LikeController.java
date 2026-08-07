package com.periferia.social.feed.api;

import com.periferia.social.feed.application.LikePost;
import com.periferia.social.feed.application.UnlikePost;
import com.periferia.social.feed.infrastructure.AuthenticatedUser;
import com.periferia.social.feed.infrastructure.LikeWebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@Tag(name = "Likes", description = "Registro y retirada de likes, con difusión en tiempo real")
public class LikeController {

    private final LikePost likePost;
    private final UnlikePost unlikePost;
    private final LikeWebSocketHandler webSocket;

    public LikeController(LikePost likePost, UnlikePost unlikePost,
                          LikeWebSocketHandler webSocket) {
        this.likePost = likePost;
        this.unlikePost = unlikePost;
        this.webSocket = webSocket;
    }

    @PostMapping
    @Operation(summary = "Da like. Devuelve 409 si es tu propia publicación o si ya diste like.")
    public LikeResponse like(@PathVariable UUID postId,
                             @AuthenticationPrincipal AuthenticatedUser user) {
        var post = likePost.execute(postId, user.id());

        // Se difunde solo si la operación tuvo éxito: un like rechazado por el
        // dominio lanza antes de llegar aquí y no genera evento.
        webSocket.broadcast(post.id(), post.likeCount());

        return new LikeResponse(post.id(), post.likeCount(), true);
    }

    @DeleteMapping
    @Operation(summary = "Retira el like. Es idempotente.")
    public LikeResponse unlike(@PathVariable UUID postId,
                               @AuthenticationPrincipal AuthenticatedUser user) {
        var post = unlikePost.execute(postId, user.id());

        webSocket.broadcast(post.id(), post.likeCount());

        return new LikeResponse(post.id(), post.likeCount(), false);
    }
}
