package com.orderfirst.api.model;

import java.util.List;
import java.util.Objects;

/**
 * Offre vendue, recopiée dans le message publié : options actives (incluses comprises), remise,
 * Open ML appliqué et mensualités calculées au jour de la vente.
 */
public class OfferSnapshot {
    private String id;
    private String label;
    private double price;
    private List<OfferOption> options;
    private Promo promo;
    private OpenMlApplied openMl;
    private double monthlyPrice;
    /** Mensualité pendant la remise ; null sans remise. */
    private Double monthlyPriceDuringPromo;

    public OfferSnapshot() {
    }

    public OfferSnapshot(final String id, final String label, final double price, final List<OfferOption> options, final Promo promo, final OpenMlApplied openMl, final double monthlyPrice, final Double monthlyPriceDuringPromo) {
        this.id = id;
        this.label = label;
        this.price = price;
        this.options = options;
        this.promo = promo;
        this.openMl = openMl;
        this.monthlyPrice = monthlyPrice;
        this.monthlyPriceDuringPromo = monthlyPriceDuringPromo;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(final double price) {
        this.price = price;
    }

    public List<OfferOption> getOptions() {
        return options;
    }

    public void setOptions(final List<OfferOption> options) {
        this.options = options;
    }

    public Promo getPromo() {
        return promo;
    }

    public void setPromo(final Promo promo) {
        this.promo = promo;
    }

    public OpenMlApplied getOpenMl() {
        return openMl;
    }

    public void setOpenMl(final OpenMlApplied openMl) {
        this.openMl = openMl;
    }

    public double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(final double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public Double getMonthlyPriceDuringPromo() {
        return monthlyPriceDuringPromo;
    }

    public void setMonthlyPriceDuringPromo(final Double monthlyPriceDuringPromo) {
        this.monthlyPriceDuringPromo = monthlyPriceDuringPromo;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OfferSnapshot that
                && Objects.equals(id, that.id) && Objects.equals(label, that.label) && Double.compare(price, that.price) == 0 && Objects.equals(options, that.options) && Objects.equals(promo, that.promo) && Objects.equals(openMl, that.openMl) && Double.compare(monthlyPrice, that.monthlyPrice) == 0 && Objects.equals(monthlyPriceDuringPromo, that.monthlyPriceDuringPromo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, price, options, promo, openMl, monthlyPrice, monthlyPriceDuringPromo);
    }

    @Override
    public String toString() {
        return "OfferSnapshot[" + "id=" + id + ", " + "label=" + label + ", " + "price=" + price + ", " + "options=" + options + ", " + "promo=" + promo + ", " + "openMl=" + openMl + ", " + "monthlyPrice=" + monthlyPrice + ", " + "monthlyPriceDuringPromo=" + monthlyPriceDuringPromo + "]";
    }
}
