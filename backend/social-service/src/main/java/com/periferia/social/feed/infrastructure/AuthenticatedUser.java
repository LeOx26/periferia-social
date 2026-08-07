package com.periferia.social.feed.infrastructure;

import java.util.UUID;

/**
 * Identidad extraída de los claims del JWT.
 *
 * El alias viaja dentro del token precisamente para que este servicio pueda sellar
 * la autoría de una publicación sin consultar nunca a auth-service ni a su base.
 */
public record AuthenticatedUser(UUID id, String alias) {}
