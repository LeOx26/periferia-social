package com.periferia.social.auth.application;

import com.periferia.social.auth.domain.User;
import com.periferia.social.auth.domain.UserRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class GetProfile {

    private final UserRepository repository;

    public GetProfile(UserRepository repository) {
        this.repository = repository;
    }

    public User execute(UUID userId) {
        return repository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }
}
