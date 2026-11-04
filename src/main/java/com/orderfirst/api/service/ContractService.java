package com.orderfirst.api.service;

import com.orderfirst.api.model.Flow;
import com.orderfirst.api.model.NumberMode;
import com.orderfirst.api.model.OrderRequest;
import com.orderfirst.api.model.SimType;
import com.orderfirst.api.domain.ReferenceData;
import com.orderfirst.api.domain.ApiException;
import com.orderfirst.api.domain.Rules;
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
    private final RhocPort rhoc;
    private final EpcPort epc;
    private final DocumentPort documents;
    private final Clock clock;

    public ContractService(final OrderService orders, final RhocPort rhoc,
                           final EpcPort epc, final DocumentPort documents, final Clock clock) {
        this.orders = orders;
        this.rhoc = rhoc;
        this.epc = epc;
        this.documents = documents;
        this.clock = clock;
    }

    public String generate(final OrderRequest request) {
        if (request != null && !Rules.requiresContract(request.getFlow())) {
            throw ApiException.rule("Aucun contrat n'est généré pour une modification d'options.");
        }
        final var checked = orders.validate(request, false);
        final var title = "Contrat - " + label(request.getFlow());
        return documents.store(request.getCustomerId(), epc.generateContract(title, lines(request, checked)));
    }

    public Optional<byte[]> find(final String documentId) {
        return documents.find(documentId);
    }

    private List<String> lines(final OrderRequest r, final OrderService.Checked c) {
        final var customer = c.customer();
        final var identity = r.getFlow() == Flow.acquisition ? r.getHolder() : customer.getIdentity();
        final var lines = new ArrayList<String>();
        lines.add("Objet : " + label(r.getFlow()));
        lines.add("Client : " + customer.getId() + " - " + identity.getCivilite() + " " + identity.getPrenom() + " " + identity.getNom());
        lines.add(customer.getCompany() != null
                ? "Entreprise : " + customer.getCompany().getRaisonSociale() + " - SIREN " + customer.getCompany().getSiren()
                : "Pièce : " + identity.getPieceType() + " " + identity.getPieceNumero());
        lines.add("Ligne : " + c.msisdn());
        if (r.getFlow() == Flow.acquisition) {
            final var a = r.getBillingAddress();
            lines.add("Adresse de facturation : " + a.getLigne() + ", " + a.getCodePostal() + " " + a.getCommune());
            lines.add(r.getNumber().getMode() == NumberMode.portabilite
                    ? "Portabilité depuis " + r.getNumber().getOperator() + " le " + r.getNumber().getPortingDate()
                    : "Nouveau numéro");
        }
        if (c.offer() != null) {
            final var options = Rules.selectedOptions(c.offer(), r.getOptionsOrEmpty());
            final boolean openMl = !Rules.isBlank(r.getOpenMlSponsor()) && Rules.isOpenMlAvailable(c.offer(), c.line())
                    && rhoc.isValidSponsor(r.getOpenMlSponsor());
            if (c.line() != null) {
                lines.add("Offre actuelle : " + c.line().getOfferLabel());
            }
            lines.add("Offre : " + c.offer().getLabel() + " - " + euro(c.offer().getPrice()) + "/mois");
            options.forEach(o -> lines.add("Option : " + o.getLabel() + " - " + (o.isIncluded() ? "incluse" : euro(o.getPrice()) + "/mois")));
            if (c.offer().getPromo() != null) {
                lines.add("Remise : " + c.offer().getPromo().getLabel());
            }
            if (openMl) {
                lines.add("Remise Open ML : -" + euro(Rules.openMlDiscount(c.offer())) + "/mois, à vie");
            }
            final double monthly = Rules.monthly(c.offer(), r.getOptionsOrEmpty(), openMl);
            lines.add("Mensualité : " + euro(monthly) + "/mois");
            final var duringPromo = Rules.monthlyDuringPromo(c.offer(), monthly);
            if (duringPromo != null) {
                lines.add("Les " + c.offer().getPromo().getDurationMonths() + " premiers mois : " + euro(duringPromo) + "/mois");
            }
        }
        if (r.getSim() != null) {
            lines.add("SIM : " + (r.getSim().getType() == SimType.esim ? "eSIM" : "SIM physique") + " - ICCID " + Rules.compact(r.getSim().getIccid()));
        }
        if (r.getFlow() == Flow.sim) {
            ReferenceData.simReasons().stream().filter(x -> x.getId().equals(r.getSimReason())).findFirst()
                    .ifPresent(reason -> lines.add("Raison : " + reason.getLabel()));
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
