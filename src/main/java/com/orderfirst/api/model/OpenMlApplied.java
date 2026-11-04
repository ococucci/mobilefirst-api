package com.orderfirst.api.model;

import java.util.Objects;

/** Remise Open ML appliquée à une commande. */
public class OpenMlApplied {
    private String sponsor;
    private double monthlyDiscount;

    public OpenMlApplied() {
    }

    public OpenMlApplied(final String sponsor, final double monthlyDiscount) {
        this.sponsor = sponsor;
        this.monthlyDiscount = monthlyDiscount;
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
                && Objects.equals(sponsor, that.sponsor) && Double.compare(monthlyDiscount, that.monthlyDiscount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sponsor, monthlyDiscount);
    }

    @Override
    public String toString() {
        return "OpenMlApplied[" + "sponsor=" + sponsor + ", " + "monthlyDiscount=" + monthlyDiscount + "]";
    }
}
