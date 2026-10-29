package com.orderfirst.api.domain;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** Erreur métier renvoyée au front sous forme de ProblemDetail (RFC 7807). */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String title;
    private final Map<String, String> fieldErrors;

    public ApiException(final HttpStatus status, final String title, final String detail, final Map<String, String> fieldErrors) {
        super(detail);
        this.status = status;
        this.title = title;
        this.fieldErrors = fieldErrors;
    }

    public static ApiException notFound(final String detail) {
        return new ApiException(HttpStatus.NOT_FOUND, "Introuvable", detail, Map.of());
    }

    /** Saisie invalide : un message par champ. */
    public static ApiException invalidFields(final Map<String, String> errors) {
        return new ApiException(HttpStatus.BAD_REQUEST, "Saisie invalide", "Corrigez les champs en rouge pour continuer.", Map.copyOf(errors));
    }

    /** Règle de gestion qui interdit l'opération (score rouge, commande en cours…). */
    public static ApiException rule(final String detail) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "Opération impossible", detail, Map.of());
    }

    public HttpStatus status() {
        return status;
    }

    public String title() {
        return title;
    }

    public Map<String, String> fieldErrors() {
        return fieldErrors;
    }
}
