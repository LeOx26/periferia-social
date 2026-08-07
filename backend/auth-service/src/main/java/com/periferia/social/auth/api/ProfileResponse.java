package com.periferia.social.auth.api;

import com.periferia.social.auth.domain.User;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponse(UUID id, String username, String firstName,
                              String lastName, LocalDate birthDate, String alias) {

    public static ProfileResponse from(User user) {
        return new ProfileResponse(user.id(), user.username(), user.firstName(),
                                   user.lastName(), user.birthDate(), user.alias());
    }
}
