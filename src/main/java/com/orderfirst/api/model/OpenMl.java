package com.orderfirst.api.model;

import java.util.Objects;

/**
 * Remise Open ML d'une offre (−5 € par mois pour une offre d'entrée de gamme, −10 € sinon), décrite comme une promo :
 * un code et un libellé affichés sur la vignette de l'offre.
 */
public class OpenMl {
    private boolean eligible;
    /** Code de la remise (ex. « OPENML_10E ») ; null si l'offre n'est pas éligible. */
    private String id;
    /** Libellé affiché sur l'offre (ex. « Open ML −10 €/mois ») ; null si l'offre n'est pas éligible. */
    private String label;
    /** Montant mensuel ; null si l'offre n'est pas éligible. */
    private Double monthlyDiscount;

    public OpenMl() {
    }

    public OpenMl(final boolean eligible, final String id, final String label, final Double monthlyDiscount) {
        this.eligible = eligible;
        this.id = id;
        this.label = label;
        this.monthlyDiscount = monthlyDiscount;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(final boolean eligible) {
        this.eligible = eligible;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public Double getMonthlyDiscount() {
        return monthlyDiscount;
    }

    public void setMonthlyDiscount(final Double monthlyDiscount) {
        this.monthlyDiscount = monthlyDiscount;
    }

    /** Offre non éligible (Carte Prépayée). */
    public static OpenMl none() {
        return new OpenMl(false, null, null, null);
    }

    public static OpenMl of(final String id, final String label, final double monthlyDiscount) {
        return new OpenMl(true, id, label, monthlyDiscount);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OpenMl that
                && eligible == that.eligible && Objects.equals(id, that.id) && Objects.equals(label, that.label) && Objects.equals(monthlyDiscount, that.monthlyDiscount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eligible, id, label, monthlyDiscount);
    }

    @Override
    public String toString() {
        return "OpenMl[" + "eligible=" + eligible + ", " + "id=" + id + ", " + "label=" + label + ", " + "monthlyDiscount=" + monthlyDiscount + "]";
    }
}
