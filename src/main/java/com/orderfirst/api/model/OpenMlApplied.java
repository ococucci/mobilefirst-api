package com.orderfirst.api.model;

import java.util.Objects;

/** Remise Open ML appliquée à une commande : code et libellé de la remise, N° sponsor et montant mensuel. */
public class OpenMlApplied {
    private String id;
    private String label;
    private String sponsor;
    private double monthlyDiscount;

    public OpenMlApplied() {
    }

    public OpenMlApplied(final String id, final String label, final String sponsor, final double monthlyDiscount) {
        this.id = id;
        this.label = label;
        this.sponsor = sponsor;
        this.monthlyDiscount = monthlyDiscount;
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

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(final String sponsor) {
        this.sponsor = sponsor;
    }

    public double getMonthlyDiscount() {
        return monthlyDiscount;
    }

    public void setMonthlyDiscount(final double monthlyDiscount) {
        this.monthlyDiscount = monthlyDiscount;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OpenMlApplied that
                && Objects.equals(id, that.id) && Objects.equals(label, that.label) && Objects.equals(sponsor, that.sponsor) && Double.compare(monthlyDiscount, that.monthlyDiscount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, sponsor, monthlyDiscount);
    }

    @Override
    public String toString() {
        return "OpenMlApplied[" + "id=" + id + ", " + "label=" + label + ", " + "sponsor=" + sponsor + ", " + "monthlyDiscount=" + monthlyDiscount + "]";
    }
}
