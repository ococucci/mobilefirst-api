package com.orderfirst.api.port;

import com.orderfirst.api.domain.Model.Promo;

import java.util.List;

/** api-promo advice : remises applicables aux offres éligibles. */
public interface PromoAdvicePort {
    List<Promo> promos();
}
