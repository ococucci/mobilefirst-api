package com.orderfirst.api.model;

import java.util.List;
import java.util.Objects;

/** Modification d'options : options ajoutées et retirées, et variation de la mensualité. */
public class OptionsChange {
    private List<OfferOption> added;
    private List<OfferOption> removed;
    private double monthlyDelta;

    public OptionsChange() {
    }

    public OptionsChange(final List<OfferOption> added, final List<OfferOption> removed, final double monthlyDelta) {
        this.added = added;
        this.removed = removed;
        this.monthlyDelta = monthlyDelta;
    }

    public List<OfferOption> getAdded() {
        return added;
    }

    public void setAdded(final List<OfferOption> added) {
        this.added = added;
    }

    public List<OfferOption> getRemoved() {
        return removed;
    }

    public void setRemoved(final List<OfferOption> removed) {
        this.removed = removed;
    }

    public double getMonthlyDelta() {
        return monthlyDelta;
    }

    public void setMonthlyDelta(final double monthlyDelta) {
        this.monthlyDelta = monthlyDelta;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OptionsChange that
                && Objects.equals(added, that.added) && Objects.equals(removed, that.removed) && Double.compare(monthlyDelta, that.monthlyDelta) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(added, removed, monthlyDelta);
    }

    @Override
    public String toString() {
        return "OptionsChange[" + "added=" + added + ", " + "removed=" + removed + ", " + "monthlyDelta=" + monthlyDelta + "]";
    }
}
