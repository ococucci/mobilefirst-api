package com.orderfirst.api.model;

import java.util.Objects;

/**
 * Résultat de la mise à jour à l'étape Client : score, Préventel relancé ou non, et client
 * (inchangé si le score est rouge).
 */
public class CustomerUpdate {
    private Score score;
    private boolean rescored;
    private Customer customer;

    public CustomerUpdate() {
    }

    public CustomerUpdate(final Score score, final boolean rescored, final Customer customer) {
        this.score = score;
        this.rescored = rescored;
        this.customer = customer;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(final Score score) {
        this.score = score;
    }

    public boolean isRescored() {
        return rescored;
    }

    public void setRescored(final boolean rescored) {
        this.rescored = rescored;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final CustomerUpdate that
                && Objects.equals(score, that.score) && rescored == that.rescored && Objects.equals(customer, that.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, rescored, customer);
    }

    @Override
    public String toString() {
        return "CustomerUpdate[" + "score=" + score + ", " + "rescored=" + rescored + ", " + "customer=" + customer + "]";
    }
}
