package com.periferia.social.feed.api;

import com.periferia.social.feed.domain.DuplicateLikeException;
import com.periferia.social.feed.domain.InvalidPostMessageException;
import com.periferia.social.feed.domain.PostNotFoundException;
import com.periferia.social.feed.domain.SelfLikeNotAllowedException;
import com.periferia.social.feed.infrastructure.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Duplica deliberadamente al de auth-service: extraer esto a una librería común
 * acoplaría el despliegue de ambos servicios. Son ~40 líneas a cambio de que cada
 * servicio pueda evolucionar su contrato de errores por separado.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String BASE_TYPE = "https://periferia.social/errors/";

    @ExceptionHandler(SelfLikeNotAllowedException.class)
    ProblemDetail handleSelfLike(SelfLikeNotAllowedException e) {
        return problem(HttpStatus.CONFLICT, "self-like-not-allowed", e.getMessage(),
                       "Un autor no puede dar like a su propia publicación.");
    }

    @ExceptionHandler(DuplicateLikeException.class)
    ProblemDetail handleDuplicateLike(DuplicateLikeException e) {
        return problem(HttpStatus.CONFLICT, "duplicate-like", e.getMessage(),
                       "Ya registraste un like en esta publicación.");
    }

    @ExceptionHandler(InvalidPostMessageException.class)
    ProblemDetail handleInvalidMessage(InvalidPostMessageException e) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-post-message", e.getMessage(), e.getMessage());
    }

    @ExceptionHandler(PostNotFoundException.class)
    ProblemDetail handlePostNotFound(PostNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "post-not-found",
                       "Publicación no encontrada", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
            .map(field -> field.getField() + ": " + field.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "validation-failed",
                       "Datos de entrada inválidos", detail);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception e) {
        // Se registra el detalle completo pero no se devuelve: nunca filtrar el stacktrace.
        log.error("Error no controlado", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error",
                       "Error interno del servidor",
                       "Ha ocurrido un error inesperado. Reporta el correlationId a soporte.");
    }

    private ProblemDetail problem(HttpStatus status, String slug, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(BASE_TYPE + slug));
        problem.setTitle(title);
        problem.setDetail(detail);

        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }

        return problem;
    }
}
