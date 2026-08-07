package com.periferia.social.auth.api;

import com.periferia.social.auth.domain.User;

import java.util.UUID;

public record LoginResponse(String accessToken, long expiresIn, UserSummary user) {

    public record UserSummary(UUID id, String alias, String firstName, String lastName) {}

    public static LoginResponse from(String token, long expiresIn, User user) {
        return new LoginResponse(token, expiresIn,
            new UserSummary(user.id(), user.alias(), user.firstName(), user.lastName()));
    }
}
