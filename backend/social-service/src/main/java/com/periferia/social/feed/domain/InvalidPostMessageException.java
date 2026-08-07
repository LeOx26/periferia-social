package com.periferia.social.feed.domain;

public class InvalidPostMessageException extends RuntimeException {

    public InvalidPostMessageException(String message) {
        super(message);
    }
}
