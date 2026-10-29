package com.orderfirst.api.port;

import com.orderfirst.api.domain.Model.MobileOption;
import com.orderfirst.api.domain.Model.Offer;

import java.util.List;

/** Catalogue Castor : familles, forfaits (arbre) et options mobile. */
public interface CataloguePort {
    List<Offer> offers();

    List<MobileOption> options();
}
