package com.orderfirst.api.model;

import java.util.Objects;

/**
 * Remise Open ML appliquée à une commande : code et libellé de la remise, sponsor (N° de mobile ou ND, nom, prénom
 * donnés par api RHOC), identifiant du jeton RHOC de la vérification et montant mensuel.
 */
public class OpenMlApplied {
    private String id;
    private String label;
    private String sponsor;
    private String sponsorLastName;
    private String sponsorFirstName;
    private String tokenId;
    private double monthlyDiscount;

    public OpenMlApplied() {
    }

    public OpenMlApplied(final String id, final String label, final String sponsor, final String sponsorLastName,
                         final String sponsorFirstName, final String tokenId, final double monthlyDiscount) {
        this.id = id;
        this.label = label;
        this.sponsor = sponsor;
        this.sponsorLastName = sponsorLastName;
        this.sponsorFirstName = sponsorFirstName;
        this.tokenId = tokenId;
        this.monthlyDiscount = monthlyDiscount;
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

    /** N° de mobile ou ND du sponsor, au format national. */
    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(final String sponsor) {
        this.sponsor = sponsor;
    }

    public String getSponsorLastName() {
        return sponsorLastName;
    }

    public void setSponsorLastName(final String sponsorLastName) {
        this.sponsorLastName = sponsorLastName;
    }

    public String getSponsorFirstName() {
        return sponsorFirstName;
    }

    public void setSponsorFirstName(final String sponsorFirstName) {
        this.sponsorFirstName = sponsorFirstName;
    }

    /** Identifiant du jeton émis par api RHOC lors de la vérification du sponsor. */
    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(final String tokenId) {
        this.tokenId = tokenId;
    }

    public double getMonthlyDiscount() {
        return monthlyDiscount;
    }

    public void setMonthlyDiscount(final double monthlyDiscount) {
        this.monthlyDiscount = monthlyDiscount;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OpenMlApplied that
                && Objects.equals(id, that.id) && Objects.equals(label, that.label) && Objects.equals(sponsor, that.sponsor)
                && Objects.equals(sponsorLastName, that.sponsorLastName) && Objects.equals(sponsorFirstName, that.sponsorFirstName)
                && Objects.equals(tokenId, that.tokenId) && Double.compare(monthlyDiscount, that.monthlyDiscount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, sponsor, sponsorLastName, sponsorFirstName, tokenId, monthlyDiscount);
    }

    @Override
    public String toString() {
        return "OpenMlApplied[" + "id=" + id + ", " + "label=" + label + ", " + "sponsor=" + sponsor + ", "
                + "sponsorLastName=" + sponsorLastName + ", " + "sponsorFirstName=" + sponsorFirstName + ", "
                + "tokenId=" + tokenId + ", " + "monthlyDiscount=" + monthlyDiscount + "]";
    }
}
