package com.orderfirst.api.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Modèle de terminal vendable (enabler si-financier) : prix pour chaque niveau d'offre et couleurs proposées. */
public class Terminal {
    private String id;
    private String brand;
    private String model;
    /** Prix du terminal (en euros) pour chaque niveau d'offre. */
    private Map<OfferLevel, Double> prices;
    private List<TerminalColor> colors;

    public Terminal() {
    }

    public Terminal(final String id, final String brand, final String model, final Map<OfferLevel, Double> prices, final List<TerminalColor> colors) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.prices = prices;
        this.colors = colors;
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

    public Map<OfferLevel, Double> getPrices() {
        return prices;
    }

    public void setPrices(final Map<OfferLevel, Double> prices) {
        this.prices = prices;
    }

    public List<TerminalColor> getColors() {
        return colors;
    }

    public void setColors(final List<TerminalColor> colors) {
        this.colors = colors;
    }

    /** Couleur proposée pour ce modèle. */
    public Optional<TerminalColor> color(final String colorId) {
        return colors == null ? Optional.empty() : colors.stream().filter(c -> c.getId().equals(colorId)).findFirst();
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Terminal that
                && Objects.equals(id, that.id) && Objects.equals(brand, that.brand) && Objects.equals(model, that.model) && Objects.equals(prices, that.prices) && Objects.equals(colors, that.colors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, brand, model, prices, colors);
    }

    @Override
    public String toString() {
        return "Terminal[" + "id=" + id + ", " + "brand=" + brand + ", " + "model=" + model + ", " + "prices=" + prices + ", " + "colors=" + colors + "]";
    }
}
