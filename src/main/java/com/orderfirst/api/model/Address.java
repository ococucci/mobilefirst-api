package com.orderfirst.api.model;

import java.util.Objects;

/**
 * Adresse (de facturation, ou du siège pour un client entreprise), fiabilisée par Api-ORAS : numéro et nom de la
 * voie, code postal et commune viennent de l'adresse choisie ; le complément est saisi librement.
 */
public class Address {
    /** Numéro dans la voie (ex. « 12 », « 3 bis ») ; vide pour une voie ou un lieu-dit sans numéro. */
    private String numeroVoie;
    /** Type et nom de la voie (ex. « rue des Flamboyants »). */
    private String nomVoie;
    private String codePostal;
    private String commune;
    private String complement;

    public Address() {
    }

    public Address(final String numeroVoie, final String nomVoie, final String codePostal, final String commune, final String complement) {
        this.numeroVoie = numeroVoie;
        this.nomVoie = nomVoie;
        this.codePostal = codePostal;
        this.commune = commune;
        this.complement = complement;
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

    public String getComplement() {
        return complement;
    }

    public void setComplement(final String complement) {
        this.complement = complement;
    }

    /** Numéro et nom de la voie, pour l'affichage (ex. « 12 rue des Flamboyants »). */
    public String voie() {
        return ((numeroVoie == null ? "" : numeroVoie.trim()) + " " + (nomVoie == null ? "" : nomVoie.trim())).trim();
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Address that
                && Objects.equals(numeroVoie, that.numeroVoie) && Objects.equals(nomVoie, that.nomVoie) && Objects.equals(codePostal, that.codePostal) && Objects.equals(commune, that.commune) && Objects.equals(complement, that.complement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroVoie, nomVoie, codePostal, commune, complement);
    }

    @Override
    public String toString() {
        return "Address[" + "numeroVoie=" + numeroVoie + ", " + "nomVoie=" + nomVoie + ", " + "codePostal=" + codePostal + ", " + "commune=" + commune + ", " + "complement=" + complement + "]";
    }
}
