package com.orderfirst.api.model;

import java.util.Objects;

/** Remise commerciale appliquée automatiquement à une offre (api-promo advice) ; les remises ne se cumulent pas. */
public class Promo {
    private String id;
    private String label;
    private double monthlyDiscount;
    private int durationMonths;

    public Promo() {
    }

    public Promo(final String id, final String label, final double monthlyDiscount, final int durationMonths) {
        this.id = id;
        this.label = label;
        this.monthlyDiscount = monthlyDiscount;
        this.durationMonths = durationMonths;
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

    public double getMonthlyDiscount() {
        return monthlyDiscount;
    }

    public void setMonthlyDiscount(final double monthlyDiscount) {
        this.monthlyDiscount = monthlyDiscount;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(final int durationMonths) {
        this.durationMonths = durationMonths;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Promo that
                && Objects.equals(id, that.id) && Objects.equals(label, that.label) && Double.compare(monthlyDiscount, that.monthlyDiscount) == 0 && durationMonths == that.durationMonths;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, monthlyDiscount, durationMonths);
    }

    @Override
    public String toString() {
        return "Promo[" + "id=" + id + ", " + "label=" + label + ", " + "monthlyDiscount=" + monthlyDiscount + ", " + "durationMonths=" + durationMonths + "]";
    }
}
