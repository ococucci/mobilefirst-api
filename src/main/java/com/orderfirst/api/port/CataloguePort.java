package com.orderfirst.api.port;

import com.orderfirst.api.model.Offer;

import java.util.List;

/** Catalogue Castor : forfaits (arbre), avec pour chacun ses options et son éligibilité Open ML. */
public interface CataloguePort {
    /**
     * Offres sans leurs remises, ajoutées par api-promo advice.
     *
     * @param territoire territoire de l'adresse du client (GUADELOUPE, MARTINIQUE, GUYANE FRANCAISE, ST-MARTIN,
     *                   ST-BARTHELEMY, FRANCE) pour ne proposer que les offres vendables sur ce territoire ;
     *                   null pour toutes les offres
     */
    List<Offer> offers(String territoire);
}
