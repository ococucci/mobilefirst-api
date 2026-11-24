package com.orderfirst.api.port;

import com.orderfirst.api.model.Country;

import java.util.List;

/** Référentiel des pays : liste proposée pour la nationalité d'origine du client. */
public interface CountryPort {

    /** Pays triés par libellé. */
    List<Country> countries();

    default boolean exists(final String id) {
        return id != null && countries().stream().anyMatch(c -> c.id().equals(id));
    }
}
