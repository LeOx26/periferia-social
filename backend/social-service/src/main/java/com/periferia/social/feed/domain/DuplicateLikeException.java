package com.periferia.social.feed.domain;

public class DuplicateLikeException extends RuntimeException {

    public DuplicateLikeException() {
        super("Ya diste like a esta publicación");
    }
}
