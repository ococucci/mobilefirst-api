package com.orderfirst.api.model;

import java.util.Objects;

/** Entreprise d'un client professionnel. */
public class Company {
    private String raisonSociale;
    private String siren;
    private String siret;

    public Company() {
    }

    public Company(final String raisonSociale, final String siren, final String siret) {
        this.raisonSociale = raisonSociale;
        this.siren = siren;
        this.siret = siret;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(final String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(final String siren) {
        this.siren = siren;
    }

    public String getSiret() {
        return siret;
    }

    public void setSiret(final String siret) {
        this.siret = siret;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Company that
                && Objects.equals(raisonSociale, that.raisonSociale) && Objects.equals(siren, that.siren) && Objects.equals(siret, that.siret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raisonSociale, siren, siret);
    }

    @Override
    public String toString() {
        return "Company[" + "raisonSociale=" + raisonSociale + ", " + "siren=" + siren + ", " + "siret=" + siret + "]";
    }
}
