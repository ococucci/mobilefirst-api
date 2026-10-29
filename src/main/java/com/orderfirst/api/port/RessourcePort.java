package com.orderfirst.api.port;

import java.util.List;

/** api ressource : numéros de mobile disponibles. */
public interface RessourcePort {
    List<String> availableNumbers();

    /** Réserve le numéro choisi pour une acquisition ; false s'il n'est plus disponible. */
    boolean reserve(String msisdn);
}
