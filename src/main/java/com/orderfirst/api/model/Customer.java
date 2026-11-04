package com.orderfirst.api.model;

import java.util.List;
import java.util.Objects;

/** Client identifié (Party) avec son score Préventel et ses lignes. */
public class Customer {
    private String id;
    private CustomerType type;
    private Identity identity;
    /** Renseignée pour un client professionnel. */
    private Company company;
    private Address address;
    private Score score;
    private List<Line> lines;

    public Customer() {
    }

    public Customer(final String id, final CustomerType type, final Identity identity, final Company company, final Address address, final Score score, final List<Line> lines) {
        this.id = id;
        this.type = type;
        this.identity = identity;
        this.company = company;
        this.address = address;
        this.score = score;
        this.lines = lines;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public CustomerType getType() {
        return type;
    }

    public void setType(final CustomerType type) {
        this.type = type;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(final Identity identity) {
        this.identity = identity;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(final Company company) {
        this.company = company;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(final Score score) {
        this.score = score;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(final List<Line> lines) {
        this.lines = lines;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final Customer that
                && Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(identity, that.identity) && Objects.equals(company, that.company) && Objects.equals(address, that.address) && Objects.equals(score, that.score) && Objects.equals(lines, that.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, identity, company, address, score, lines);
    }

    @Override
    public String toString() {
        return "Customer[" + "id=" + id + ", " + "type=" + type + ", " + "identity=" + identity + ", " + "company=" + company + ", " + "address=" + address + ", " + "score=" + score + ", " + "lines=" + lines + "]";
    }
}
