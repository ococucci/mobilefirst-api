package com.orderfirst.api.port;

import com.orderfirst.api.model.IbanToken;

import java.util.Optional;

/** api IML : contrôle et stockage du mandat SEPA (signé hors de l'outil), tokenisation de l'IBAN. */
public interface ImlPort {

    /** Enregistre le mandat et renvoie l'information bancaire tokenisée. */
    IbanToken storeMandate(String holder, String iban);

    /** Information bancaire tokenisée enregistrée sous cet identifiant ; vide si elle est inconnue. */
    Optional<IbanToken> token(String uid);
}
