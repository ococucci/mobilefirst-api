package com.orderfirst.api.service;

import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.model.Offer;
import com.orderfirst.api.port.CataloguePort;
import com.orderfirst.api.port.PromoAdvicePort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Offres complètes : catalogue Castor (offres et options) et remise api-promo advice. */
@Service
public class CatalogService {
    private final CataloguePort catalogue;
    private final PromoAdvicePort promos;

    public CatalogService(final CataloguePort catalogue, final PromoAdvicePort promos) {
        this.catalogue = catalogue;
        this.promos = promos;
    }

    /** Offres vendables sur le territoire de l'adresse du client (toutes si le territoire n'est pas précisé). */
    public List<Offer> offers(final String territoire) {
        return catalogue.offers(Rules.isBlank(territoire) ? null : territoire.trim()).stream().map(this::withPromo).toList();
    }

    /** Offre par identifiant, quel que soit le territoire. */
    public Optional<Offer> offer(final String id) {
        return catalogue.offers(null).stream().filter(o -> o.getId().equals(id)).findFirst()
                .map(this::withPromo);
    }

    private Offer withPromo(final Offer offer) {
        return offer.withPromo(promos.promoFor(offer.getId()).orElse(null));
    }
}
