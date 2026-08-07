package com.periferia.social.auth.domain;

/**
 * Un único mensaje para "no existe el usuario" y "la contraseña no coincide":
 * distinguirlos permitiría enumerar qué usuarios existen en el sistema.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Usuario o contraseña incorrectos");
    }
}
