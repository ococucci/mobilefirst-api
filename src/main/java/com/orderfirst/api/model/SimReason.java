package com.orderfirst.api.model;

import java.util.Objects;

/** Raison d'un changement de SIM. */
public class SimReason {
    private String id;
    private String label;
    /** Frais de 10 € à régler en caisse (perte et vol uniquement). */
    private boolean fee;

    public SimReason() {
    }

    public SimReason(final String id, final String label, final boolean fee) {
        this.id = id;
        this.label = label;
        this.fee = fee;
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

    public boolean isFee() {
        return fee;
    }

    public void setFee(final boolean fee) {
        this.fee = fee;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final SimReason that
                && Objects.equals(id, that.id) && Objects.equals(label, that.label) && fee == that.fee;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, fee);
    }

    @Override
    public String toString() {
        return "SimReason[" + "id=" + id + ", " + "label=" + label + ", " + "fee=" + fee + "]";
    }
}
