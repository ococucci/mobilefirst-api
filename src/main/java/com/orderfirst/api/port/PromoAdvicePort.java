package com.orderfirst.api.port;

import com.orderfirst.api.domain.Model.Promo;

import java.util.Optional;

/** api-promo advice : remise applicable à une offre éligible (une seule, appliquée automatiquement). */
public interface PromoAdvicePort {
    Optional<Promo> promoFor(String offerId);
}
