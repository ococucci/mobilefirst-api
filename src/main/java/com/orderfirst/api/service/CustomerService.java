package com.orderfirst.api.service;

import com.orderfirst.api.domain.ApiException;
import com.orderfirst.api.domain.Model.Address;
import com.orderfirst.api.domain.Model.Customer;
import com.orderfirst.api.domain.Model.Identity;
import com.orderfirst.api.domain.Model.Line;
import com.orderfirst.api.domain.Model.Score;
import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.domain.Validation;
import com.orderfirst.api.port.OrderPort;
import com.orderfirst.api.port.PartyPort;
import com.orderfirst.api.port.ScoringPort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Identification du client (écrans 1 à 1f) et mise à jour de ses informations (écran Client). */
@Service
public class CustomerService {
    private final PartyPort party;
    private final ScoringPort scoring;
    private final OrderPort orders;
    private final Clock clock;

    public CustomerService(final PartyPort party, final ScoringPort scoring, final OrderPort orders, final Clock clock) {
        this.party = party;
        this.scoring = scoring;
        this.orders = orders;
        this.clock = clock;
    }

    public Customer byMsisdn(final String msisdn) {
        if (!Rules.isValidMsisdn(msisdn)) {
            new Validation().check(true, "msisdn", Validation.MSISDN_MESSAGE).throwIfInvalid();
        }
        return party.findByMsisdn(msisdn).orElseThrow(() -> ApiException.notFound("Aucun client pour le numéro " + Rules.digits(msisdn) + "."));
    }

    public Customer byId(final String id) {
        return party.findById(id).orElseThrow(() -> ApiException.notFound("Client " + id + " introuvable."));
    }

    /** Recherche d'existence avant la création d'un client sans numéro. */
    public Optional<Customer> findExisting(final Identity identity) {
        new Validation().identity("identity", identity, false, today()).throwIfInvalid();
        return party.findExisting(identity);
    }

    public record Creation(Score score, Customer customer) {
    }

    /** Contrôle Préventel puis création du client ; pas de création si le score est rouge. */
    public Creation create(final Identity identity, final Address address) {
        new Validation().identity("identity", identity, false, today()).throwIfInvalid();
        final var score = scoring.preventel(identity);
        if (score == Score.rouge) {
            return new Creation(score, null);
        }
        return new Creation(score, party.create(identity, address == null ? new Address("", "", "", "") : address, score));
    }

    public record Update(Score score, boolean rescored, Customer customer) {
    }

    /**
     * Mise à jour à l'étape Client. Le nom, le prénom ou la date de naissance modifiés relancent Préventel ;
     * avec un score rouge, rien n'est enregistré.
     */
    public Update update(final String id, final Identity identity, final Address address) {
        final var current = byId(id);
        new Validation().identity("identity", identity, true, today()).address("address", address).throwIfInvalid();
        final boolean rescore = Rules.identityChangeRequiresRescore(current.identity(), identity);
        final var score = rescore ? scoring.preventel(identity) : current.score();
        if (score == Score.rouge) {
            return new Update(score, rescore, current);
        }
        return new Update(score, rescore, party.update(id, identity, address, score));
    }

    /** Lignes du client ayant une commande en cours (api-order) : elles ne peuvent pas être modifiées. */
    public List<String> pendingLines(final String id) {
        return orders.linesWithPendingOrder(byId(id).lines().stream().map(Line::msisdn).toList());
    }

    /** Re-scoring Préventel d'une ligne Card avant un changement de forfait. */
    public Score rescoreCardLine(final String id, final String msisdn) {
        final var customer = byId(id);
        final var line = line(customer, msisdn);
        if (!line.isCard()) {
            throw ApiException.rule("Seule une ligne Card prépayée est re-scorée avant un changement de forfait.");
        }
        return scoring.rescore(customer);
    }

    public static Line line(final Customer customer, final String msisdn) {
        final var n = Rules.digits(msisdn);
        return customer.lines().stream().filter(l -> l.msisdn().equals(n)).findFirst()
                .orElseThrow(() -> ApiException.notFound("La ligne " + n + " n'appartient pas au client " + customer.id() + "."));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
