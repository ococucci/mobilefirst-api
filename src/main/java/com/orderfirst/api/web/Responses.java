package com.orderfirst.api.web;

import org.springframework.http.ResponseEntity;

import java.util.Optional;

/**
 * Une recherche sans résultat répond 204 (No Content) plutôt que 404 : ce n'est pas une erreur.
 * Le 404 reste réservé aux actions qui portent sur un élément inconnu (commande, encaissement…).
 */
final class Responses {
    private Responses() {
    }

    static <T> ResponseEntity<T> okOrNoContent(final Optional<T> result) {
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
