package com.periferia.social.feed.domain;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(UUID id) {
        super("No existe la publicación " + id);
    }
}
