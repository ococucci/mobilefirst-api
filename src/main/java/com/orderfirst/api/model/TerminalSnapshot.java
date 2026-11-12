package com.orderfirst.api.model;

import java.util.Objects;

/** Terminal vendu, recopié dans le message publié : modèle, couleur, niveau de l'offre et prix appliqué. */
public class TerminalSnapshot {
    private String id;
    private String brand;
    private String model;
    private TerminalColor color;
    private OfferLevel level;
    private double price;
    private String imei;

    public TerminalSnapshot() {
    }

    public TerminalSnapshot(final String id, final String brand, final String model, final TerminalColor color, final OfferLevel level, final double price, final String imei) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.level = level;
        this.price = price;
        this.imei = imei;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public TerminalColor getColor() {
        return color;
    }

    public void setColor(final TerminalColor color) {
        this.color = color;
    }

    public OfferLevel getLevel() {
        return level;
    }

    public void setLevel(final OfferLevel level) {
        this.level = level;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(final double price) {
        this.price = price;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(final String imei) {
        this.imei = imei;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final TerminalSnapshot that
                && Objects.equals(id, that.id) && Objects.equals(brand, that.brand) && Objects.equals(model, that.model) && Objects.equals(color, that.color) && level == that.level && Double.compare(price, that.price) == 0 && Objects.equals(imei, that.imei);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, brand, model, color, level, price, imei);
    }

    @Override
    public String toString() {
        return "TerminalSnapshot[" + "id=" + id + ", " + "brand=" + brand + ", " + "model=" + model + ", " + "color=" + color + ", " + "level=" + level + ", " + "price=" + price + ", " + "imei=" + imei + "]";
    }
}
