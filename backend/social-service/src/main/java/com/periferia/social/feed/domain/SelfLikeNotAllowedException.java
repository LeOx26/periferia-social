package com.periferia.social.feed.domain;

public class SelfLikeNotAllowedException extends RuntimeException {

    public SelfLikeNotAllowedException() {
        super("No puedes dar like a tu propia publicación");
    }
}
