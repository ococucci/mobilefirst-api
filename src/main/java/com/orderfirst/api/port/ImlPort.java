package com.orderfirst.api.port;

/** api IML : contrôle et stockage du mandat SEPA (signé hors de l'outil). */
public interface ImlPort {
    /** Renvoie la référence unique du mandat (RUM). */
    String storeMandate(String holder, String iban);

    boolean exists(String mandateRef);
}
