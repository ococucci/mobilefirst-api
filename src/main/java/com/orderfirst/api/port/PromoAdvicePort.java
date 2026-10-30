package com.orderfirst.api.port;

import com.orderfirst.api.domain.Model.Promo;

import java.util.List;

/** api-promo advice : remises applicables à une offre éligible (appliquées automatiquement). */
public interface PromoAdvicePort {
    List<Promo> promosFor(String offerId);
}
