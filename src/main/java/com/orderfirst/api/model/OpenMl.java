package com.orderfirst.api.model;

import java.util.Objects;

/** Remise Open ML d'une offre : −5 € par mois pour une offre d'entrée de gamme, −10 € sinon. */
public class OpenMl {
    private boolean eligible;
    /** Montant mensuel ; null si l'offre n'est pas éligible. */
    private Double monthlyDiscount;

    public OpenMl() {
    }

    public OpenMl(final boolean eligible, final Double monthlyDiscount) {
        this.eligible = eligible;
        this.monthlyDiscount = monthlyDiscount;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(final boolean eligible) {
        this.eligible = eligible;
    }

    public Double getMonthlyDiscount() {
        return monthlyDiscount;
    }

    public void setMonthlyDiscount(final Double monthlyDiscount) {
        this.monthlyDiscount = monthlyDiscount;
    }

    /** Offre non éligible (Carte Prépayée). */
    public static OpenMl none() {
        return new OpenMl(false, null);
    }

    public static OpenMl of(final double monthlyDiscount) {
        return new OpenMl(true, monthlyDiscount);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OpenMl that
                && eligible == that.eligible && Objects.equals(monthlyDiscount, that.monthlyDiscount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eligible, monthlyDiscount);
    }

    @Override
    public String toString() {
        return "OpenMl[" + "eligible=" + eligible + ", " + "monthlyDiscount=" + monthlyDiscount + "]";
    }
}
