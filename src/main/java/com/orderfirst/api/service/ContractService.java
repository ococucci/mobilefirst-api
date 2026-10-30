package com.orderfirst.api.service;

import com.orderfirst.api.domain.ApiException;
import com.orderfirst.api.domain.Model;
import com.orderfirst.api.domain.Model.Flow;
import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.port.CataloguePort;
import com.orderfirst.api.port.DocumentPort;
import com.orderfirst.api.port.EpcPort;
import com.orderfirst.api.port.RhocPort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Contrat : généré par api EPC à partir de la commande contrôlée, puis stocké par Api-document. */
@Service
public class ContractService {
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRANCE);

    private final OrderService orders;
    private final CataloguePort catalogue;
    private final RhocPort rhoc;
    private final EpcPort epc;
    private final DocumentPort documents;
    private final Clock clock;

    public ContractService(final OrderService orders, final CataloguePort catalogue, final RhocPort rhoc,
                           final EpcPort epc, final DocumentPort documents, final Clock clock) {
        this.orders = orders;
        this.catalogue = catalogue;
        this.rhoc = rhoc;
        this.epc = epc;
        this.documents = documents;
        this.clock = clock;
    }

    public String generate(final OrderRequest request) {
        if (request != null && !Rules.requiresContract(request.flow())) {
            throw ApiException.rule("Aucun contrat n'est généré pour une modification d'options.");
        }
        final var checked = orders.validate(request, false);
        final var title = "Contrat - " + label(request.flow());
        return documents.store(request.customerId(), epc.generateContract(title, lines(request, checked)));
    }

    public Optional<byte[]> find(final String documentId) {
        return documents.find(documentId);
    }

    private List<String> lines(final OrderRequest r, final OrderService.Checked c) {
        final var customer = c.customer();
        final var identity = r.flow() == Flow.acquisition ? r.holder() : customer.identity();
        final var lines = new ArrayList<String>();
        lines.add("Objet : " + label(r.flow()));
        lines.add("Client : " + customer.id() + " - " + identity.civilite() + " " + identity.prenom() + " " + identity.nom());
        lines.add(customer.company() != null
                ? "Entreprise : " + customer.company().raisonSociale() + " - SIREN " + customer.company().siren()
                : "Pièce : " + identity.pieceType() + " " + identity.pieceNumero());
        lines.add("Ligne : " + c.msisdn());
        if (r.flow() == Flow.acquisition) {
            final var a = r.billingAddress();
            lines.add("Adresse de facturation : " + a.ligne() + ", " + a.codePostal() + " " + a.commune());
            lines.add(r.number().mode() == OrderRequest.NumberChoice.Mode.portabilite
                    ? "Portabilité depuis " + r.number().operator() + " le " + r.number().portingDate()
                    : "Nouveau numéro");
        }
        if (c.offer() != null) {
            final var options = catalogue.options().stream().filter(o -> r.optionsOrEmpty().contains(o.id())).toList();
            final boolean openMl = !Rules.isBlank(r.openMlSponsor()) && Rules.isOpenMlAvailable(c.offer(), c.line())
                    && rhoc.isValidSponsor(r.openMlSponsor());
            if (c.line() != null) {
                lines.add("Offre actuelle : " + c.line().offerLabel());
            }
            lines.add("Offre : " + c.offer().label() + " - " + euro(c.offer().price()) + "/mois");
            options.forEach(o -> lines.add("Option : " + o.label() + " - " + euro(o.price()) + "/mois"));
            if (openMl) {
                lines.add("Remise Open ML : -" + euro(Rules.openMlDiscount(c.offer())) + "/mois, à vie");
            }
            lines.add("Mensualité : " + euro(Rules.monthly(c.offer(), options, openMl)) + "/mois");
        }
        if (r.sim() != null) {
            lines.add("SIM : " + (r.sim().type() == Model.SimType.esim ? "eSIM" : "SIM physique") + " - ICCID " + Rules.compact(r.sim().iccid()));
        }
        if (r.flow() == Flow.sim) {
            Model.SIM_REASONS.stream().filter(x -> x.id().equals(r.simReason())).findFirst()
                    .ifPresent(reason -> lines.add("Raison : " + reason.label()));
        }
        if (c.cashDue() > 0) {
            lines.add("A régler en caisse : " + euro(c.cashDue()));
        }
        lines.add("");
        lines.add("Édité le " + LocalDateTime.now(clock).format(WHEN) + " par Orange Order First.");
        lines.add("");
        lines.add("Signature du client :");
        return lines;
    }

    private static String label(final Flow flow) {
        return switch (flow) {
            case acquisition -> "Nouvelle ligne";
            case forfait -> "Changement de forfait";
            case options -> "Changement d'option";
            case sim -> "Changement de SIM";
        };
    }

    private static String euro(final double amount) {
        return String.format(Locale.FRANCE, "%.2f EUR", amount);
    }
}
