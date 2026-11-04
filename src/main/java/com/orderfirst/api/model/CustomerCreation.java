package com.orderfirst.api.model;

import java.util.Objects;

/** Résultat de la création d'un client : score Préventel et client créé (null si score rouge). */
public class CustomerCreation {
    private Score score;
    private Customer customer;

    public CustomerCreation() {
    }

    public CustomerCreation(final Score score, final Customer customer) {
        this.score = score;
        this.customer = customer;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(final Score score) {
        this.score = score;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final CustomerCreation that
                && Objects.equals(score, that.score) && Objects.equals(customer, that.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, customer);
    }

    @Override
    public String toString() {
        return "CustomerCreation[" + "score=" + score + ", " + "customer=" + customer + "]";
    }
}
