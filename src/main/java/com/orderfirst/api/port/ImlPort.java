package com.orderfirst.api.port;

import com.orderfirst.api.model.SepaMandate;

import java.util.Optional;

/** api IML : contrôle et stockage du mandat SEPA (signé hors de l'outil), tokenisation de l'IBAN. */
public interface ImlPort {

    /** Enregistre le mandat : renvoie sa référence unique (RUM) et l'information bancaire tokenisée. */
    SepaMandate storeMandate(String holder, String iban);

    /** Mandat enregistré sous cette référence ; vide s'il est inconnu. */
    Optional<SepaMandate> mandate(String mandateRef);

    default boolean exists(final String mandateRef) {
        return mandate(mandateRef).isPresent();
    }
}
