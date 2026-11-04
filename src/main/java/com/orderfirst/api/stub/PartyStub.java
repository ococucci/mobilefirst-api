package com.orderfirst.api.stub;

import com.orderfirst.api.model.Address;
import com.orderfirst.api.model.Customer;
import com.orderfirst.api.model.CustomerType;
import com.orderfirst.api.model.Identity;
import com.orderfirst.api.model.Score;
import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.port.PartyPort;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Stub
class PartyStub implements PartyPort {
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    PartyStub() {
        StubData.customers().forEach(c -> customers.put(c.getId(), c));
    }

    @Override
    public Optional<Customer> findByMsisdn(final String msisdn) {
        final var n = Rules.digits(msisdn);
        return customers.values().stream().filter(c -> c.getLines().stream().anyMatch(l -> l.getMsisdn().equals(n))).findFirst();
    }

    @Override
    public Optional<Customer> findById(final String id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public Optional<Customer> findExisting(final Identity identity) {
        return customers.values().stream()
                .filter(c -> norm(c.getIdentity().getNom()).equals(norm(identity.getNom()))
                        && norm(c.getIdentity().getPrenom()).equals(norm(identity.getPrenom()))
                        && c.getIdentity().getDateNaissance().equals(identity.getDateNaissance())
                        && Rules.compact(c.getIdentity().getPieceNumero()).equals(Rules.compact(identity.getPieceNumero())))
                .findFirst();
    }

    @Override
    public synchronized Customer create(final Identity identity, final Address address, final Score score) {
        final var id = "CL-%06d".formatted(900000 + customers.size());
        final var customer = new Customer(id, CustomerType.GP, identity, null, address, score, List.of());
        customers.put(id, customer);
        return customer;
    }

    @Override
    public Customer update(final String id, final Identity identity, final Address address, final Score score) {
        final var updated = customers.computeIfPresent(id, (k, c) -> {
            c.setIdentity(identity);
            c.setAddress(address);
            c.setScore(score);
            return c;
        });
        if (updated == null) {
            throw new NoSuchElementException("Client " + id + " introuvable");
        }
        return updated;
    }

    private static String norm(final String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
