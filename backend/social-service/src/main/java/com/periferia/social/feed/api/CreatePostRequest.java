package com.periferia.social.feed.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solo el mensaje. El autor sale del JWT y la fecha la pone el servidor: aceptar
 * cualquiera de los dos del cliente permitiría publicar en nombre de otro o
 * falsear el orden del feed.
 */
public record CreatePostRequest(
    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 280, message = "El mensaje no puede superar los 280 caracteres")
    String message
) {}
