package com.orderfirst.api.model;

import java.util.Objects;

/** Terminal vendable (enabler si-financier). */
public class Terminal {
    private String id;
    private String label;
    private double price;

    public Terminal() {
    }

    public Terminal(final String id, final String label, final double price) {
        this.id = id;
        this.label = label;
        this.price = price;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(final double price) {
        this.price = price;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Terminal that
                && Objects.equals(id, that.id) && Objects.equals(label, that.label) && Double.compare(price, that.price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, price);
    }

    @Override
    public String toString() {
        return "Terminal[" + "id=" + id + ", " + "label=" + label + ", " + "price=" + price + "]";
    }
}
