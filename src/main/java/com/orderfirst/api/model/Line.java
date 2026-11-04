package com.orderfirst.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/** Ligne mobile d'un client. */
public class Line {
    private String msisdn;
    private String offerId;
    private String offerLabel;
    /** Mensualité ; null pour une ligne Card prépayée (sans mensualité). */
    private Double monthlyPrice;
    /** Ligne Card prépayée. Exposée en JSON sous le nom « isCard ». */
    private boolean card;
    private SimType simType;
    /** Identifiants des options actives. */
    private List<String> options;

    public Line() {
    }

    public Line(final String msisdn, final String offerId, final String offerLabel, final Double monthlyPrice, final boolean card, final SimType simType, final List<String> options) {
        this.msisdn = msisdn;
        this.offerId = offerId;
        this.offerLabel = offerLabel;
        this.monthlyPrice = monthlyPrice;
        this.card = card;
        this.simType = simType;
        this.options = options;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(final String msisdn) {
        this.msisdn = msisdn;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(final String offerId) {
        this.offerId = offerId;
    }

    public String getOfferLabel() {
        return offerLabel;
    }

    public void setOfferLabel(final String offerLabel) {
        this.offerLabel = offerLabel;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(final Double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    @JsonProperty("isCard")
    public boolean isCard() {
        return card;
    }

    @JsonProperty("isCard")
    public void setCard(final boolean card) {
        this.card = card;
    }

    public SimType getSimType() {
        return simType;
    }

    public void setSimType(final SimType simType) {
        this.simType = simType;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(final List<String> options) {
        this.options = options;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Line that
                && Objects.equals(msisdn, that.msisdn) && Objects.equals(offerId, that.offerId) && Objects.equals(offerLabel, that.offerLabel) && Objects.equals(monthlyPrice, that.monthlyPrice) && card == that.card && Objects.equals(simType, that.simType) && Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msisdn, offerId, offerLabel, monthlyPrice, card, simType, options);
    }

    @Override
    public String toString() {
        return "Line[" + "msisdn=" + msisdn + ", " + "offerId=" + offerId + ", " + "offerLabel=" + offerLabel + ", " + "monthlyPrice=" + monthlyPrice + ", " + "card=" + card + ", " + "simType=" + simType + ", " + "options=" + options + "]";
    }
}
