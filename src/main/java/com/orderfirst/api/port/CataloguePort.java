package com.orderfirst.api.port;

import com.orderfirst.api.domain.Model.Offer;

import java.util.List;

/** Catalogue Castor : forfaits (arbre), avec pour chacun ses options et son éligibilité Open ML. */
public interface CataloguePort {
    /** Offres sans leurs remises, ajoutées par api-promo advice. */
    List<Offer> offers();
}
