package com.orderfirst.api.model;

import java.util.Objects;

/** Titulaire : identité du client et pièce d'identité. */
public class Identity {
    private String civilite;
    private String nom;
    private String prenom;
    /** Format ISO yyyy-mm-dd. */
    private String dateNaissance;
    private String departementNaissance;
    private String nationalite;
    private PieceType pieceType;
    private String pieceNumero;
    private String mobileContact;
    private String email;

    public Identity() {
    }

    public Identity(final String civilite, final String nom, final String prenom, final String dateNaissance, final String departementNaissance, final String nationalite, final PieceType pieceType, final String pieceNumero, final String mobileContact, final String email) {
        this.civilite = civilite;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.departementNaissance = departementNaissance;
        this.nationalite = nationalite;
        this.pieceType = pieceType;
        this.pieceNumero = pieceNumero;
        this.mobileContact = mobileContact;
        this.email = email;
    }

    public String getCivilite() {
        return civilite;
    }

    public void setCivilite(final String civilite) {
        this.civilite = civilite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(final String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(final String prenom) {
        this.prenom = prenom;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(final String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getDepartementNaissance() {
        return departementNaissance;
    }

    public void setDepartementNaissance(final String departementNaissance) {
        this.departementNaissance = departementNaissance;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(final String nationalite) {
        this.nationalite = nationalite;
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public void setPieceType(final PieceType pieceType) {
        this.pieceType = pieceType;
    }

    public String getPieceNumero() {
        return pieceNumero;
    }

    public void setPieceNumero(final String pieceNumero) {
        this.pieceNumero = pieceNumero;
    }

    public String getMobileContact() {
        return mobileContact;
    }

    public void setMobileContact(final String mobileContact) {
        this.mobileContact = mobileContact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Identity that
                && Objects.equals(civilite, that.civilite) && Objects.equals(nom, that.nom) && Objects.equals(prenom, that.prenom) && Objects.equals(dateNaissance, that.dateNaissance) && Objects.equals(departementNaissance, that.departementNaissance) && Objects.equals(nationalite, that.nationalite) && Objects.equals(pieceType, that.pieceType) && Objects.equals(pieceNumero, that.pieceNumero) && Objects.equals(mobileContact, that.mobileContact) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(civilite, nom, prenom, dateNaissance, departementNaissance, nationalite, pieceType, pieceNumero, mobileContact, email);
    }

    @Override
    public String toString() {
        return "Identity[" + "civilite=" + civilite + ", " + "nom=" + nom + ", " + "prenom=" + prenom + ", " + "dateNaissance=" + dateNaissance + ", " + "departementNaissance=" + departementNaissance + ", " + "nationalite=" + nationalite + ", " + "pieceType=" + pieceType + ", " + "pieceNumero=" + pieceNumero + ", " + "mobileContact=" + mobileContact + ", " + "email=" + email + "]";
    }
}
