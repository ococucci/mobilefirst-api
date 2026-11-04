package com.orderfirst.api.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Message publié dans le bus PubSub à la validation de la commande. L'offre vendue (acquisition,
 * changement de forfait) ou les options modifiées y sont recopiées avec libellés et prix du jour de la vente.
 */
public class OrderValidated {
    private String topic;
    private String orderNumber;
    private Instant createdAt;
    private Flow flow;
    private String customerId;
    private String msisdn;
    private double cashDue;
    private OfferSnapshot offer;
    private OptionsChange optionsChange;
    private OrderRequest order;

    public OrderValidated() {
    }

    public OrderValidated(final String topic, final String orderNumber, final Instant createdAt, final Flow flow, final String customerId, final String msisdn, final double cashDue, final OfferSnapshot offer, final OptionsChange optionsChange, final OrderRequest order) {
        this.topic = topic;
        this.orderNumber = orderNumber;
        this.createdAt = createdAt;
        this.flow = flow;
        this.customerId = customerId;
        this.msisdn = msisdn;
        this.cashDue = cashDue;
        this.offer = offer;
        this.optionsChange = optionsChange;
        this.order = order;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
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

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(final Flow flow) {
        this.flow = flow;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(final String msisdn) {
        this.msisdn = msisdn;
    }

    public double getCashDue() {
        return cashDue;
    }

    public void setCashDue(final double cashDue) {
        this.cashDue = cashDue;
    }

    public OfferSnapshot getOffer() {
        return offer;
    }

    public void setOffer(final OfferSnapshot offer) {
        this.offer = offer;
    }

    public OptionsChange getOptionsChange() {
        return optionsChange;
    }

    public void setOptionsChange(final OptionsChange optionsChange) {
        this.optionsChange = optionsChange;
    }

    public OrderRequest getOrder() {
        return order;
    }

    public void setOrder(final OrderRequest order) {
        this.order = order;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final OrderValidated that
                && Objects.equals(topic, that.topic) && Objects.equals(orderNumber, that.orderNumber) && Objects.equals(createdAt, that.createdAt) && Objects.equals(flow, that.flow) && Objects.equals(customerId, that.customerId) && Objects.equals(msisdn, that.msisdn) && Double.compare(cashDue, that.cashDue) == 0 && Objects.equals(offer, that.offer) && Objects.equals(optionsChange, that.optionsChange) && Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, orderNumber, createdAt, flow, customerId, msisdn, cashDue, offer, optionsChange, order);
    }

    @Override
    public String toString() {
        return "OrderValidated[" + "topic=" + topic + ", " + "orderNumber=" + orderNumber + ", " + "createdAt=" + createdAt + ", " + "flow=" + flow + ", " + "customerId=" + customerId + ", " + "msisdn=" + msisdn + ", " + "cashDue=" + cashDue + ", " + "offer=" + offer + ", " + "optionsChange=" + optionsChange + ", " + "order=" + order + "]";
    }
}
