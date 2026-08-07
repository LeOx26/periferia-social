package com.periferia.social.feed.api;

import java.util.UUID;

/**
 * Único tipo de mensaje que viaja por el WebSocket. Que el payload sea tan simple
 * es lo que hace innecesario STOMP: no hay que enrutar por destinos ni negociar
 * suscripciones, solo difundir un contador.
 */
public record LikeEvent(String type, UUID postId, int likeCount) {

    public static LikeEvent updated(UUID postId, int likeCount) {
        return new LikeEvent("LIKE_UPDATED", postId, likeCount);
    }
}
