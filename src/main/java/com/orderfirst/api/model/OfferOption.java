package com.orderfirst.api.model;

import java.util.List;
import java.util.Objects;

/**
 * Option proposée avec une offre ; une option incluse est gratuite et toujours active. Les options
 * incompatibles ne peuvent pas être actives en même temps qu'elle.
 */
public class OfferOption {
    private String id;
    private String label;
    private double price;
    private boolean included;
    /**
     * Identifiants des options incompatibles avec celle-ci. L'incompatibilité vaut dans les deux sens, même si elle
     * n'est déclarée que sur l'une des deux options.
     */
    private List<String> incompatibleOptions = List.of();

    public OfferOption() {
    }

    public OfferOption(final String id, final String label, final double price, final boolean included) {
        this(id, label, price, included, List.of());
    }

    public OfferOption(final String id, final String label, final double price, final boolean included, final List<String> incompatibleOptions) {
        this.id = id;
        this.label = label;
        this.price = price;
        this.included = included;
        setIncompatibleOptions(incompatibleOptions);
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

    public List<String> getIncompatibleOptions() {
        return incompatibleOptions;
    }

    public void setIncompatibleOptions(final List<String> incompatibleOptions) {
        this.incompatibleOptions = incompatibleOptions == null ? List.of() : incompatibleOptions;
    }

    /** Les deux options s'excluent : l'incompatibilité déclarée sur l'une vaut pour l'autre. */
    public boolean isIncompatibleWith(final OfferOption other) {
        return !id.equals(other.id) && (incompatibleOptions.contains(other.id) || other.incompatibleOptions.contains(id));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OfferOption that
                && Objects.equals(id, that.id) && Objects.equals(label, that.label) && Double.compare(price, that.price) == 0 && included == that.included && Objects.equals(incompatibleOptions, that.incompatibleOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, price, included, incompatibleOptions);
    }

    @Override
    public String toString() {
        return "OfferOption[" + "id=" + id + ", " + "label=" + label + ", " + "price=" + price + ", " + "included=" + included + ", " + "incompatibleOptions=" + incompatibleOptions + "]";
    }
}
