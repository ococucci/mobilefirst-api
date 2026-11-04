package com.orderfirst.api.model;

import java.util.Objects;

/** Option proposée avec une offre ; une option incluse est gratuite et toujours active. */
public class OfferOption {
    private String id;
    private String label;
    private double price;
    private boolean included;

    public OfferOption() {
    }

    public OfferOption(final String id, final String label, final double price, final boolean included) {
        this.id = id;
        this.label = label;
        this.price = price;
        this.included = included;
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

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(final boolean included) {
        this.included = included;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OfferOption that
                && Objects.equals(id, that.id) && Objects.equals(label, that.label) && Double.compare(price, that.price) == 0 && included == that.included;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, price, included);
    }

    @Override
    public String toString() {
        return "OfferOption[" + "id=" + id + ", " + "label=" + label + ", " + "price=" + price + ", " + "included=" + included + "]";
    }
}
