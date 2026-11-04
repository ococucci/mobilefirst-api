package com.orderfirst.api.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Offre du catalogue avec ce qui en dépend : éligibilité Open ML, options proposées (prix propres à
 * l'offre, éventuellement incluses) et remise appliquée automatiquement (une seule par offre).
 */
public class Offer {
    private String id;
    private FamilyId family;
    private String label;
    private String detail;
    /** Prix mensuel en euros (0 pour la Carte Prépayée). */
    private double price;
    private OpenMl openMl;
    private List<OfferOption> options;
    /** Remise de l'offre ; null si elle n'en a pas. */
    private Promo promo;

    public Offer() {
    }

    public Offer(final String id, final FamilyId family, final String label, final String detail, final double price, final OpenMl openMl, final List<OfferOption> options, final Promo promo) {
        this.id = id;
        this.family = family;
        this.label = label;
        this.detail = detail;
        this.price = price;
        this.openMl = openMl;
        this.options = options;
        this.promo = promo;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public FamilyId getFamily() {
        return family;
    }

    public void setFamily(final FamilyId family) {
        this.family = family;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(final String detail) {
        this.detail = detail;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(final double price) {
        this.price = price;
    }

    public OpenMl getOpenMl() {
        return openMl;
    }

    public void setOpenMl(final OpenMl openMl) {
        this.openMl = openMl;
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

    /** Option proposée avec cette offre. */
    public Optional<OfferOption> option(final String optionId) {
        return options == null ? Optional.empty() : options.stream().filter(o -> o.getId().equals(optionId)).findFirst();
    }

    /** Copie de l'offre avec la remise donnée. */
    public Offer withPromo(final Promo promo) {
        return new Offer(id, family, label, detail, price, openMl, options, promo);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Offer that
                && Objects.equals(id, that.id) && Objects.equals(family, that.family) && Objects.equals(label, that.label) && Objects.equals(detail, that.detail) && Double.compare(price, that.price) == 0 && Objects.equals(openMl, that.openMl) && Objects.equals(options, that.options) && Objects.equals(promo, that.promo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, family, label, detail, price, openMl, options, promo);
    }

    @Override
    public String toString() {
        return "Offer[" + "id=" + id + ", " + "family=" + family + ", " + "label=" + label + ", " + "detail=" + detail + ", " + "price=" + price + ", " + "openMl=" + openMl + ", " + "options=" + options + ", " + "promo=" + promo + "]";
    }
}
