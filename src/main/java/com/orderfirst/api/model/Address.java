package com.orderfirst.api.model;

import java.util.Objects;

/** Adresse (de facturation ou du siège). */
public class Address {
    private String ligne;
    private String codePostal;
    private String commune;
    private String complement;

    public Address() {
    }

    public Address(final String ligne, final String codePostal, final String commune, final String complement) {
        this.ligne = ligne;
        this.codePostal = codePostal;
        this.commune = commune;
        this.complement = complement;
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

    public String getComplement() {
        return complement;
    }

    public void setComplement(final String complement) {
        this.complement = complement;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Address that
                && Objects.equals(ligne, that.ligne) && Objects.equals(codePostal, that.codePostal) && Objects.equals(commune, that.commune) && Objects.equals(complement, that.complement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ligne, codePostal, commune, complement);
    }

    @Override
    public String toString() {
        return "Address[" + "ligne=" + ligne + ", " + "codePostal=" + codePostal + ", " + "commune=" + commune + ", " + "complement=" + complement + "]";
    }
}
