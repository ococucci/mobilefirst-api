package com.orderfirst.api.service;

import com.orderfirst.api.domain.Model.Offer;
import com.orderfirst.api.port.CataloguePort;
import com.orderfirst.api.port.PromoAdvicePort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Offres complètes : catalogue Castor (offres et options) et remises api-promo advice. */
@Service
public class CatalogService {
    private final CataloguePort catalogue;
    private final PromoAdvicePort promos;

    public CatalogService(final CataloguePort catalogue, final PromoAdvicePort promos) {
        this.catalogue = catalogue;
        this.promos = promos;
    }

    public List<Offer> offers() {
        return catalogue.offers().stream().map(o -> o.withPromos(promos.promosFor(o.id()))).toList();
    }

    public Optional<Offer> offer(final String id) {
        return catalogue.offers().stream().filter(o -> o.id().equals(id)).findFirst()
                .map(o -> o.withPromos(promos.promosFor(o.id())));
    }
}
