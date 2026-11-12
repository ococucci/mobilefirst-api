package com.orderfirst.api.model;

import java.util.Objects;

/** Couleur proposée pour un modèle de terminal. */
public class TerminalColor {
    private String id;
    private String label;

    public TerminalColor() {
    }

    public TerminalColor(final String id, final String label) {
        this.id = id;
        this.label = label;
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

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final TerminalColor that
                && Objects.equals(id, that.id) && Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label);
    }

    @Override
    public String toString() {
        return "TerminalColor[" + "id=" + id + ", " + "label=" + label + "]";
    }
}
