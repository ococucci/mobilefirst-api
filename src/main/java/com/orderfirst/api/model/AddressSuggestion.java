package com.orderfirst.api.model;

import java.util.Objects;

/** Adresse proposée par Api-ORAS : numéro et nom de la voie, code postal et commune. */
public class AddressSuggestion {
    private String numeroVoie;
    private String nomVoie;
    private String codePostal;
    private String commune;

    public AddressSuggestion() {
    }

    public AddressSuggestion(final String numeroVoie, final String nomVoie, final String codePostal, final String commune) {
        this.numeroVoie = numeroVoie;
        this.nomVoie = nomVoie;
        this.codePostal = codePostal;
        this.commune = commune;
    }

    public String getNumeroVoie() {
        return numeroVoie;
    }

    public void setNumeroVoie(final String numeroVoie) {
        this.numeroVoie = numeroVoie;
    }

    public String getNomVoie() {
        return nomVoie;
    }

    public void setNomVoie(final String nomVoie) {
        this.nomVoie = nomVoie;
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
                && Objects.equals(numeroVoie, that.numeroVoie) && Objects.equals(nomVoie, that.nomVoie) && Objects.equals(codePostal, that.codePostal) && Objects.equals(commune, that.commune);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroVoie, nomVoie, codePostal, commune);
    }

    @Override
    public String toString() {
        return "AddressSuggestion[" + "numeroVoie=" + numeroVoie + ", " + "nomVoie=" + nomVoie + ", " + "codePostal=" + codePostal + ", " + "commune=" + commune + "]";
    }
}
