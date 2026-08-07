package com.periferia.social.auth.api;

import com.periferia.social.auth.domain.InvalidCredentialsException;
import com.periferia.social.auth.infrastructure.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Todas las respuestas de error salen en formato RFC 7807 (Problem Details) con
 * el correlationId incluido, de modo que un usuario puede reportar un fallo con
 * un identificador que lleva directo a la línea de log correspondiente.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String BASE_TYPE = "https://periferia.social/errors/";

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
        log.warn("Intento de login fallido");
        return problem(HttpStatus.UNAUTHORIZED, "invalid-credentials",
                       e.getMessage(), "Verifica el usuario y la contraseña.");
    }

    @ExceptionHandler(NoSuchElementException.class)
    ProblemDetail handleNotFound(NoSuchElementException e) {
        return problem(HttpStatus.NOT_FOUND, "not-found", e.getMessage(), e.getMessage());
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
