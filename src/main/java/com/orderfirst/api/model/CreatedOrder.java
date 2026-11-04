package com.orderfirst.api.model;

import java.time.Instant;
import java.util.Objects;

/** Commande créée : numéro, date et montant à régler en caisse (calculé par l'API). */
public class CreatedOrder {
    private String orderNumber;
    private Instant createdAt;
    private double cashDue;

    public CreatedOrder() {
    }

    public CreatedOrder(final String orderNumber, final Instant createdAt, final double cashDue) {
        this.orderNumber = orderNumber;
        this.createdAt = createdAt;
        this.cashDue = cashDue;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public double getCashDue() {
        return cashDue;
    }

    public void setCashDue(final double cashDue) {
        this.cashDue = cashDue;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final CreatedOrder that
                && Objects.equals(orderNumber, that.orderNumber) && Objects.equals(createdAt, that.createdAt) && Double.compare(cashDue, that.cashDue) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, createdAt, cashDue);
    }

    @Override
    public String toString() {
        return "CreatedOrder[" + "orderNumber=" + orderNumber + ", " + "createdAt=" + createdAt + ", " + "cashDue=" + cashDue + "]";
    }
}
