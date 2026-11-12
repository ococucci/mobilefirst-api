package com.orderfirst.api.model;

import java.util.Objects;

/** Terminal ajouté à une nouvelle ligne : modèle, couleur et IMEI. Le prix est celui du niveau de l'offre. */
public class TerminalChoice {
    private String id;
    private String colorId;
    private String imei;

    public TerminalChoice() {
    }

    public TerminalChoice(final String id, final String colorId, final String imei) {
        this.id = id;
        this.colorId = colorId;
        this.imei = imei;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getColorId() {
        return colorId;
    }

    public void setColorId(final String colorId) {
        this.colorId = colorId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(final String imei) {
        this.imei = imei;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final TerminalChoice that
                && Objects.equals(id, that.id) && Objects.equals(colorId, that.colorId) && Objects.equals(imei, that.imei);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, colorId, imei);
    }

    @Override
    public String toString() {
        return "TerminalChoice[" + "id=" + id + ", " + "colorId=" + colorId + ", " + "imei=" + imei + "]";
    }
}
