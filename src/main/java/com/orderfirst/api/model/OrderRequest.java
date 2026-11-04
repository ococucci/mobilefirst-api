package com.orderfirst.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Objects;

/**
 * Commande saisie dans le tunnel, corps de POST /contracts et POST /orders. Les champs utiles dépendent
 * de la demande ({@code flow}) :
 * <ul>
 *   <li>acquisition : holder, billingAddress, number, offerId, options, openMlSponsor, sim, terminal, sepaMandateRef ;</li>
 *   <li>forfait : lineMsisdn, offerId, options, openMlSponsor ;</li>
 *   <li>options : lineMsisdn, options (liste complète souhaitée, le serveur calcule ajouts et retraits) ;</li>
 *   <li>sim : lineMsisdn, simReason, sim.</li>
 * </ul>
 * Contrat (sauf options) : contractRef issu de POST /contracts et contractSigned.
 */
public class OrderRequest {
    private Flow flow;
    private String customerId;
    private String lineMsisdn;
    private Identity holder;
    private Address billingAddress;
    private NumberChoice number;
    private String offerId;
    private List<String> options;
    private String openMlSponsor;
    private SimChoice sim;
    private TerminalChoice terminal;
    private String sepaMandateRef;
    private String simReason;
    private String contractRef;
    private Boolean contractSigned;
    private Boolean cgvAccepted;
    private Boolean privacyAccepted;

    public OrderRequest() {
    }

    public OrderRequest(final Flow flow, final String customerId, final String lineMsisdn, final Identity holder, final Address billingAddress, final NumberChoice number, final String offerId, final List<String> options, final String openMlSponsor, final SimChoice sim, final TerminalChoice terminal, final String sepaMandateRef, final String simReason, final String contractRef, final Boolean contractSigned, final Boolean cgvAccepted, final Boolean privacyAccepted) {
        this.flow = flow;
        this.customerId = customerId;
        this.lineMsisdn = lineMsisdn;
        this.holder = holder;
        this.billingAddress = billingAddress;
        this.number = number;
        this.offerId = offerId;
        this.options = options;
        this.openMlSponsor = openMlSponsor;
        this.sim = sim;
        this.terminal = terminal;
        this.sepaMandateRef = sepaMandateRef;
        this.simReason = simReason;
        this.contractRef = contractRef;
        this.contractSigned = contractSigned;
        this.cgvAccepted = cgvAccepted;
        this.privacyAccepted = privacyAccepted;
    }

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(final Flow flow) {
        this.flow = flow;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }

    public String getLineMsisdn() {
        return lineMsisdn;
    }

    public void setLineMsisdn(final String lineMsisdn) {
        this.lineMsisdn = lineMsisdn;
    }

    public Identity getHolder() {
        return holder;
    }

    public void setHolder(final Identity holder) {
        this.holder = holder;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(final Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public NumberChoice getNumber() {
        return number;
    }

    public void setNumber(final NumberChoice number) {
        this.number = number;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(final String offerId) {
        this.offerId = offerId;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(final List<String> options) {
        this.options = options;
    }

    public String getOpenMlSponsor() {
        return openMlSponsor;
    }

    public void setOpenMlSponsor(final String openMlSponsor) {
        this.openMlSponsor = openMlSponsor;
    }

    public SimChoice getSim() {
        return sim;
    }

    public void setSim(final SimChoice sim) {
        this.sim = sim;
    }

    public TerminalChoice getTerminal() {
        return terminal;
    }

    public void setTerminal(final TerminalChoice terminal) {
        this.terminal = terminal;
    }

    public String getSepaMandateRef() {
        return sepaMandateRef;
    }

    public void setSepaMandateRef(final String sepaMandateRef) {
        this.sepaMandateRef = sepaMandateRef;
    }

    public String getSimReason() {
        return simReason;
    }

    public void setSimReason(final String simReason) {
        this.simReason = simReason;
    }

    public String getContractRef() {
        return contractRef;
    }

    public void setContractRef(final String contractRef) {
        this.contractRef = contractRef;
    }

    public Boolean getContractSigned() {
        return contractSigned;
    }

    public void setContractSigned(final Boolean contractSigned) {
        this.contractSigned = contractSigned;
    }

    public Boolean getCgvAccepted() {
        return cgvAccepted;
    }

    public void setCgvAccepted(final Boolean cgvAccepted) {
        this.cgvAccepted = cgvAccepted;
    }

    public Boolean getPrivacyAccepted() {
        return privacyAccepted;
    }

    public void setPrivacyAccepted(final Boolean privacyAccepted) {
        this.privacyAccepted = privacyAccepted;
    }

    /** Options choisies, liste vide si absentes. */
    @JsonIgnore
    public List<String> getOptionsOrEmpty() {
        return options == null ? List.of() : options;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OrderRequest that
                && Objects.equals(flow, that.flow) && Objects.equals(customerId, that.customerId) && Objects.equals(lineMsisdn, that.lineMsisdn) && Objects.equals(holder, that.holder) && Objects.equals(billingAddress, that.billingAddress) && Objects.equals(number, that.number) && Objects.equals(offerId, that.offerId) && Objects.equals(options, that.options) && Objects.equals(openMlSponsor, that.openMlSponsor) && Objects.equals(sim, that.sim) && Objects.equals(terminal, that.terminal) && Objects.equals(sepaMandateRef, that.sepaMandateRef) && Objects.equals(simReason, that.simReason) && Objects.equals(contractRef, that.contractRef) && Objects.equals(contractSigned, that.contractSigned) && Objects.equals(cgvAccepted, that.cgvAccepted) && Objects.equals(privacyAccepted, that.privacyAccepted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flow, customerId, lineMsisdn, holder, billingAddress, number, offerId, options, openMlSponsor, sim, terminal, sepaMandateRef, simReason, contractRef, contractSigned, cgvAccepted, privacyAccepted);
    }

    @Override
    public String toString() {
        return "OrderRequest[" + "flow=" + flow + ", " + "customerId=" + customerId + ", " + "lineMsisdn=" + lineMsisdn + ", " + "holder=" + holder + ", " + "billingAddress=" + billingAddress + ", " + "number=" + number + ", " + "offerId=" + offerId + ", " + "options=" + options + ", " + "openMlSponsor=" + openMlSponsor + ", " + "sim=" + sim + ", " + "terminal=" + terminal + ", " + "sepaMandateRef=" + sepaMandateRef + ", " + "simReason=" + simReason + ", " + "contractRef=" + contractRef + ", " + "contractSigned=" + contractSigned + ", " + "cgvAccepted=" + cgvAccepted + ", " + "privacyAccepted=" + privacyAccepted + "]";
    }
}
