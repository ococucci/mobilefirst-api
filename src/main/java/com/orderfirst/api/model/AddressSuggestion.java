package com.orderfirst.api.model;

import java.util.Objects;

/** Adresse proposée par Api-ORAS. */
public class AddressSuggestion {
    private String ligne;
    private String codePostal;
    private String commune;

    public AddressSuggestion() {
    }

    public AddressSuggestion(final String ligne, final String codePostal, final String commune) {
        this.ligne = ligne;
        this.codePostal = codePostal;
        this.commune = commune;
    }

    public String getLigne() {
        return ligne;
    }

    public void setLigne(final String ligne) {
        this.ligne = ligne;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(final String codePostal) {
        this.codePostal = codePostal;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(final String commune) {
        this.commune = commune;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final AddressSuggestion that
                && Objects.equals(ligne, that.ligne) && Objects.equals(codePostal, that.codePostal) && Objects.equals(commune, that.commune);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ligne, codePostal, commune);
    }

    @Override
    public String toString() {
        return "AddressSuggestion[" + "ligne=" + ligne + ", " + "codePostal=" + codePostal + ", " + "commune=" + commune + "]";
    }
}
