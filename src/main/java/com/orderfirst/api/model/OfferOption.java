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
     * Identifiants des options incompatibles avec celle-ci : tant qu'elle est active, ces options ne peuvent pas
     * l'être. L'incompatibilité est à sens unique : seule la liste de l'option active compte.
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

    /** Cette option exclut l'autre si l'identifiant de l'autre figure dans sa liste d'incompatibilités. */
    public boolean excludes(final OfferOption other) {
        return !id.equals(other.id) && incompatibleOptions.contains(other.id);
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
