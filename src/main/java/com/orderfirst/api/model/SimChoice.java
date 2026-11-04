package com.orderfirst.api.model;

import java.util.Objects;

/** Carte SIM de la commande. */
public class SimChoice {
    private SimType type;
    private String iccid;

    public SimChoice() {
    }

    public SimChoice(final SimType type, final String iccid) {
        this.type = type;
        this.iccid = iccid;
    }

    public SimType getType() {
        return type;
    }

    public void setType(final SimType type) {
        this.type = type;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(final String iccid) {
        this.iccid = iccid;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final SimChoice that
                && Objects.equals(type, that.type) && Objects.equals(iccid, that.iccid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, iccid);
    }

    @Override
    public String toString() {
        return "SimChoice[" + "type=" + type + ", " + "iccid=" + iccid + "]";
    }
}
