package com.orderfirst.api.port;

import com.orderfirst.api.model.Sponsor;

import java.util.Optional;

/** api RHOC : recherche du sponsor Open ML (N° de mobile ou ND). */
public interface RhocPort {

    /**
     * @param msisdn N° de mobile ou ND du sponsor, au format national (0690…) ou international (590690…)
     * @return le sponsor et l'identifiant du jeton émis pour cette vérification ; vide si le N° n'est pas reconnu
     */
    Optional<Sponsor> sponsor(String msisdn);

    default boolean isValidSponsor(final String msisdn) {
        return sponsor(msisdn).isPresent();
    }
}
