package com.orderfirst.api.model;

import java.util.Objects;

/**
 * Numéro d'appel d'une nouvelle ligne : nouveau numéro choisi dans la liste, ou portabilité
 * (code RIO, numéro conservé, opérateur donneur et date de portage, tous obligatoires).
 */
public class NumberChoice {
    private NumberMode mode;
    private String msisdn;
    private String rio;
    private String portedMsisdn;
    private String operator;
    private String portingDate;

    public NumberChoice() {
    }

    public NumberChoice(final NumberMode mode, final String msisdn, final String rio, final String portedMsisdn, final String operator, final String portingDate) {
        this.mode = mode;
        this.msisdn = msisdn;
        this.rio = rio;
        this.portedMsisdn = portedMsisdn;
        this.operator = operator;
        this.portingDate = portingDate;
    }

    public NumberMode getMode() {
        return mode;
    }

    public void setMode(final NumberMode mode) {
        this.mode = mode;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(final String msisdn) {
        this.msisdn = msisdn;
    }

    public String getRio() {
        return rio;
    }

    public void setRio(final String rio) {
        this.rio = rio;
    }

    public String getPortedMsisdn() {
        return portedMsisdn;
    }

    public void setPortedMsisdn(final String portedMsisdn) {
        this.portedMsisdn = portedMsisdn;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(final String operator) {
        this.operator = operator;
    }

    public String getPortingDate() {
        return portingDate;
    }

    public void setPortingDate(final String portingDate) {
        this.portingDate = portingDate;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final NumberChoice that
                && Objects.equals(mode, that.mode) && Objects.equals(msisdn, that.msisdn) && Objects.equals(rio, that.rio) && Objects.equals(portedMsisdn, that.portedMsisdn) && Objects.equals(operator, that.operator) && Objects.equals(portingDate, that.portingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, msisdn, rio, portedMsisdn, operator, portingDate);
    }

    @Override
    public String toString() {
        return "NumberChoice[" + "mode=" + mode + ", " + "msisdn=" + msisdn + ", " + "rio=" + rio + ", " + "portedMsisdn=" + portedMsisdn + ", " + "operator=" + operator + ", " + "portingDate=" + portingDate + "]";
    }
}
