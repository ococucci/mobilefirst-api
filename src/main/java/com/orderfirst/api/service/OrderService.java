package com.orderfirst.api.service;

import com.orderfirst.api.model.NumberChoice;
import com.orderfirst.api.model.OfferOption;
import com.orderfirst.api.model.OfferSnapshot;
import com.orderfirst.api.model.OpenMlApplied;
import com.orderfirst.api.model.OptionsChange;
import com.orderfirst.api.model.CreatedOrder;
import com.orderfirst.api.model.Customer;
import com.orderfirst.api.model.CustomerType;
import com.orderfirst.api.model.FamilyId;
import com.orderfirst.api.model.Flow;
import com.orderfirst.api.model.Line;
import com.orderfirst.api.model.NumberMode;
import com.orderfirst.api.model.Offer;
import com.orderfirst.api.model.OrderRequest;
import com.orderfirst.api.model.OrderValidated;
import com.orderfirst.api.model.Score;
import com.orderfirst.api.model.Terminal;
import com.orderfirst.api.domain.ReferenceData;
import com.orderfirst.api.domain.ApiException;
import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.domain.Validation;
import com.orderfirst.api.port.DocumentPort;
import com.orderfirst.api.port.ImlPort;
import com.orderfirst.api.port.ManagePaymentPort;
import com.orderfirst.api.port.OrderEventPublisher;
import com.orderfirst.api.port.OrderPort;
import com.orderfirst.api.port.RessourcePort;
import com.orderfirst.api.port.RhocPort;
import com.orderfirst.api.port.ScoringPort;
import com.orderfirst.api.port.SiFinancierPort;
import com.orderfirst.api.port.SiFinancierPort.IccidCheck;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.orderfirst.api.domain.Rules.isBlank;

/**
 * Validation et création de la commande (écran « Vérifiez la commande »). Toutes les règles de gestion sont
 * revérifiées côté serveur : le front n'est pas une source de confiance. Les montants sont recalculés ici.
 */
@Service
public class OrderService {
    private final CustomerService customers;
    private final CatalogService catalog;
    private final RhocPort rhoc;
    private final RessourcePort ressource;
    private final ScoringPort scoring;
    private final SiFinancierPort siFinancier;
    private final ImlPort iml;
    private final DocumentPort documents;
    private final OrderPort orders;
    private final OrderEventPublisher bus;
    private final ManagePaymentPort payments;
    private final Clock clock;
    /** Montant dû en caisse par commande, pour contrôler l'encaissement. */
    private final Map<String, Double> cashDueByOrder = new ConcurrentHashMap<>();

    public OrderService(final CustomerService customers, final CatalogService catalog, final RhocPort rhoc,
                        final RessourcePort ressource, final ScoringPort scoring, final SiFinancierPort siFinancier,
                        final ImlPort iml, final DocumentPort documents, final OrderPort orders,
                        final OrderEventPublisher bus, final ManagePaymentPort payments, final Clock clock) {
        this.customers = customers;
        this.catalog = catalog;
        this.rhoc = rhoc;
        this.ressource = ressource;
        this.scoring = scoring;
        this.siFinancier = siFinancier;
        this.iml = iml;
        this.documents = documents;
        this.orders = orders;
        this.bus = bus;
        this.payments = payments;
        this.clock = clock;
    }






    public CreatedOrder create(final OrderRequest request) {
        final var checked = validate(request);
        final var offer = request.getFlow() == Flow.acquisition || request.getFlow() == Flow.forfait ? snapshot(request, checked) : null;
        final var optionsChange = request.getFlow() == Flow.options ? optionsChange(request, checked) : null;
        final var created = orders.create(checked.msisdn(), payload(request, checked, offer, optionsChange));
        if (request.getFlow() == Flow.acquisition && request.getNumber().getMode() == NumberMode.nouveau) {
            ressource.reserve(request.getNumber().getMsisdn());
        }
        if (request.getSim() != null) {
            siFinancier.consumeIccid(request.getSim().getIccid());
        }
        cashDueByOrder.put(created.getOrderNumber(), checked.cashDue());
        bus.publish(new OrderValidated(OrderEventPublisher.TOPIC, created.getOrderNumber(), created.getCreatedAt(), request.getFlow(),
                request.getCustomerId(), checked.msisdn(), checked.cashDue(), offer, optionsChange, request));
        return new CreatedOrder(created.getOrderNumber(), created.getCreatedAt(), checked.cashDue());
    }

    /** Enregistre le paiement effectué en caisse (enabler managePayment). */
    public String recordCashPayment(final String orderNumber, final Double amount) {
        final var due = cashDueByOrder.get(orderNumber);
        if (due == null) {
            throw ApiException.notFound("Commande " + orderNumber + " introuvable.");
        }
        if (due == 0) {
            throw ApiException.rule("Aucun montant n'est à régler en caisse pour cette commande.");
        }
        new Validation().check(amount == null || Math.abs(due - amount) > 0.001, "amount",
                String.format(java.util.Locale.FRANCE, "Le montant encaissé doit être de %.2f €.", due)).throwIfInvalid();
        return payments.recordCashPayment(orderNumber, amount);
    }

    /** Résultat des contrôles : numéro de la ligne concernée et montant à régler en caisse. */
    public record Checked(String msisdn, double cashDue, Customer customer, Offer offer, Line line) {
    }

    public Checked validate(final OrderRequest r) {
        return validate(r, true);
    }

    /**
     * @param withConfirmation false pour générer le contrat : la signature et les acceptations
     *                         ne sont contrôlées qu'à la validation.
     */
    public Checked validate(final OrderRequest r, final boolean withConfirmation) {
        if (r == null || r.getFlow() == null) {
            new Validation().check(true, "flow", "La demande est obligatoire.").throwIfInvalid();
        }
        if (isBlank(r.getCustomerId())) {
            new Validation().check(true, "customerId", "Le client est obligatoire.").throwIfInvalid();
        }
        final var customer = customers.byId(r.getCustomerId());
        if (customer.getScore() == Score.rouge) {
            throw ApiException.rule("Score Préventel rouge : aucune vente possible.");
        }
        final var v = new Validation();
        final var checked = switch (r.getFlow()) {
            case acquisition -> acquisition(r, customer, v);
            case forfait -> forfait(r, customer, v);
            case options -> options(r, customer, v);
            case sim -> sim(r, customer, v);
        };
        if (withConfirmation) {
            confirmation(r, v);
        }
        v.throwIfInvalid();
        return checked;
    }

    private Checked acquisition(final OrderRequest r, final Customer customer, final Validation v) {
        if (customer.getType() == CustomerType.PRO) {
            throw ApiException.rule("Client entreprise : seuls les actes de gestion sur un client existant sont autorisés.");
        }
        v.identity("holder", r.getHolder(), true, today()).address("billingAddress", r.getBillingAddress());
        number(r.getNumber(), v);
        final var offer = offer(r.getOfferId(), customer.getScore(), v);
        checkOptions(r, offer, v);
        openMl(r, offer, null, v);
        checkSim(r, v);
        double terminalPrice = 0;
        final var isCard = offer != null && offer.getFamily() == FamilyId.prepaye;
        if (r.getTerminal() != null && !isCard) {
            final var terminal = siFinancier.terminals().stream().filter(t -> t.getId().equals(r.getTerminal().getId())).findFirst();
            v.check(terminal.isEmpty(), "terminal.id", "Choisissez un terminal.");
            if (isBlank(r.getTerminal().getImei())) {
                v.check(true, "terminal.imei", "L'IMEI est obligatoire.");
            } else {
                v.check(!Rules.isValidImei(r.getTerminal().getImei()), "terminal.imei", "IMEI invalide : 15 chiffres.");
            }
            terminalPrice = terminal.map(Terminal::getPrice).orElse(0.);
        }
        if (!isCard) {
            v.check(!iml.exists(r.getSepaMandateRef()), "sepaMandateRef", "Le mandat SEPA doit être enregistré (POST /sepa-mandates).");
        }
        final var msisdn = r.getNumber() == null ? "" : Rules.digits(
                r.getNumber().getMode() == NumberMode.portabilite ? r.getNumber().getPortedMsisdn() : r.getNumber().getMsisdn());
        return new Checked(msisdn, Rules.acquisitionCashDue(offer, terminalPrice), customer, offer, null);
    }

    private Checked forfait(final OrderRequest r, final Customer customer, final Validation v) {
        final var line = modifiableLine(r, customer, v);
        var score = customer.getScore();
        if (line != null && line.isCard()) {
            score = scoring.rescore(customer);
            if (score != Score.vert) {
                throw ApiException.rule("Re-scoring Préventel " + score + " : aucun forfait ne peut être pris pour cette ligne Card.");
            }
        }
        final var offer = offer(r.getOfferId(), score, v);
        v.check(offer != null && line != null && offer.getId().equals(line.getOfferId()), "offerId", "Choisissez une offre différente de l'offre actuelle.");
        checkOptions(r, offer, v);
        openMl(r, offer, line, v);
        return new Checked(line == null ? "" : line.getMsisdn(), 0, customer, offer, line);
    }

    private Checked options(final OrderRequest r, final Customer customer, final Validation v) {
        final var line = modifiableLine(r, customer, v);
        // Les options proposées sont celles de l'offre actuelle de la ligne.
        final var offer = line == null ? null : catalog.offer(line.getOfferId()).orElse(null);
        checkOptions(r, offer, v);
        if (offer != null && !v.has("options")) {
            v.check(Objects.equals(paidOptions(offer, line.getOptions()), paidOptions(offer, r.getOptionsOrEmpty())), "options",
                    "Ajoutez ou retirez au moins une option.");
        }
        return new Checked(line == null ? "" : line.getMsisdn(), 0, customer, offer, line);
    }

    /** Options payantes choisies : une option incluse ne compte pas comme un ajout ou un retrait. */
    private static Set<String> paidOptions(final Offer offer, final List<String> ids) {
        return Rules.selectedOptions(offer, ids).stream().filter(o -> !o.isIncluded()).map(OfferOption::getId)
                .collect(java.util.stream.Collectors.toSet());
    }

    private Checked sim(final OrderRequest r, final Customer customer, final Validation v) {
        final var line = modifiableLine(r, customer, v);
        v.check(ReferenceData.simReasons().stream().noneMatch(reason -> reason.getId().equals(r.getSimReason())), "simReason",
                "Choisissez la raison du changement.");
        checkSim(r, v);
        return new Checked(line == null ? "" : line.getMsisdn(), Rules.simChangeFee(r.getSimReason()), customer, null, line);
    }

    // ----- Contrôles communs

    private Line modifiableLine(final OrderRequest r, final Customer customer, final Validation v) {
        if (isBlank(r.getLineMsisdn())) {
            v.check(true, "lineMsisdn", "Choisissez la ligne concernée.");
            return null;
        }
        final var line = CustomerService.line(customer, r.getLineMsisdn());
        if (!orders.linesWithPendingOrder(List.of(line.getMsisdn())).isEmpty()) {
            throw ApiException.rule("La ligne " + line.getMsisdn() + " a une commande en cours : elle ne peut pas être modifiée.");
        }
        return line;
    }

    private void number(final NumberChoice n, final Validation v) {
        if (n == null || n.getMode() == null) {
            v.check(true, "number.mode", "Choisissez le numéro d'appel.");
            return;
        }
        if (n.getMode() == NumberMode.nouveau) {
            if (isBlank(n.getMsisdn())) {
                v.check(true, "number.msisdn", "Choisissez un numéro dans la liste.");
            } else {
                v.check(!ressource.availableNumbers().contains(Rules.digits(n.getMsisdn())), "number.msisdn", "Ce numéro n'est plus disponible.");
            }
            return;
        }
        if (isBlank(n.getRio())) {
            v.check(true, "number.rio", "Le code RIO est obligatoire.");
        } else {
            v.check(!Rules.isValidRio(n.getRio()), "number.rio", "Code RIO invalide : 12 caractères (ex. 01P123456ABC).");
        }
        if (isBlank(n.getPortedMsisdn())) {
            v.check(true, "number.portedMsisdn", "Le numéro à conserver est obligatoire.");
        } else {
            v.check(!Rules.isValidMsisdn(n.getPortedMsisdn()), "number.portedMsisdn", Validation.MSISDN_MESSAGE);
        }
        v.check(n.getOperator() == null || !ReferenceData.DONOR_OPERATORS.contains(n.getOperator()), "number.operator", "L'opérateur donneur est obligatoire.");
        if (isBlank(n.getPortingDate())) {
            v.check(true, "number.portingDate", "La date d'exécution du portage est obligatoire.");
        } else {
            final var date = Validation.parseDate(n.getPortingDate());
            v.check(date == null || date.isBefore(today()), "number.portingDate", "La date ne peut pas être dans le passé.");
        }
    }

    private Offer offer(final String offerId, final Score score, final Validation v) {
        final var offer = offerId == null ? null : catalog.offer(offerId).orElse(null);
        if (offer == null) {
            v.check(true, "offerId", "Choisissez un forfait.");
            return null;
        }
        v.check(!Rules.allowedFamilies(score).contains(offer.getFamily()), "offerId",
                "Score " + score + " : seule l'offre Carte Prépayée est vendable.");
        return offer;
    }

    private void checkOptions(final OrderRequest r, final Offer offer, final Validation v) {
        if (offer == null) {
            return;
        }
        v.check(offer.getOptions().isEmpty() && !r.getOptionsOrEmpty().isEmpty(), "options", "Aucune option n'est proposée avec cette offre.");
        v.check(r.getOptionsOrEmpty().stream().anyMatch(o -> o == null || offer.option(o).isEmpty()), "options",
                "Option non proposée avec cette offre.");
    }

    private void openMl(final OrderRequest r, final Offer offer, final Line line, final Validation v) {
        if (isBlank(r.getOpenMlSponsor())) {
            return;
        }
        if (!Rules.isOpenMlAvailable(offer, line)) {
            v.check(offer != null, "openMlSponsor", "Remise Open ML indisponible pour la Carte Prépayée et pour une ligne Card.");
            return;
        }
        v.check(!rhoc.isValidSponsor(r.getOpenMlSponsor()), "openMlSponsor", "Sponsor non reconnu par api RHOC.");
    }

    private void checkSim(final OrderRequest r, final Validation v) {
        if (r.getSim() == null || isBlank(r.getSim().getIccid())) {
            v.check(true, "sim.iccid", "La SIM est obligatoire : saisissez ou scannez l'ICCID.");
            return;
        }
        v.check(r.getSim().getType() == null, "sim.type", "Choisissez le type de SIM.");
        switch (siFinancier.checkIccid(r.getSim().getIccid())) {
            case format -> v.check(true, "sim.iccid", "ICCID invalide : 19 ou 20 chiffres commençant par 89.");
            case inconnu -> v.check(true, "sim.iccid", "ICCID inconnu du stock boutique (si-financier).");
            case IccidCheck.ok -> {
            }
        }
    }

    private void confirmation(final OrderRequest r, final Validation v) {
        if (Rules.requiresContract(r.getFlow())) {
            v.check(!documents.exists(r.getContractRef()), "contractRef", "Le contrat doit être généré avant la validation.");
            v.check(!Boolean.TRUE.equals(r.getContractSigned()), "contractSigned", "Cochez « Le client a signé le contrat » pour valider.");
        }
        v.check(!Boolean.TRUE.equals(r.getCgvAccepted()), "cgvAccepted", "Les conditions générales de vente doivent être acceptées.");
        v.check(!Boolean.TRUE.equals(r.getPrivacyAccepted()), "privacyAccepted", "La politique de données personnelles doit être acceptée.");
    }

    private OfferSnapshot snapshot(final OrderRequest r, final Checked c) {
        final var offer = c.offer();
        final boolean openMl = !isBlank(r.getOpenMlSponsor()) && Rules.isOpenMlAvailable(offer, c.line());
        final double monthly = Rules.monthly(offer, r.getOptionsOrEmpty(), openMl);
        return new OfferSnapshot(offer.getId(), offer.getLabel(), offer.getPrice(), Rules.selectedOptions(offer, r.getOptionsOrEmpty()),
                offer.getPromo(), openMl ? new OpenMlApplied(Rules.digits(r.getOpenMlSponsor()), Rules.openMlDiscount(offer)) : null,
                monthly, Rules.monthlyDuringPromo(offer, monthly));
    }

    private OptionsChange optionsChange(final OrderRequest r, final Checked c) {
        final var current = paidOptions(c.offer(), c.line().getOptions());
        final var wanted = paidOptions(c.offer(), r.getOptionsOrEmpty());
        final var added = c.offer().getOptions().stream().filter(o -> wanted.contains(o.getId()) && !current.contains(o.getId())).toList();
        final var removed = c.offer().getOptions().stream().filter(o -> current.contains(o.getId()) && !wanted.contains(o.getId())).toList();
        final double delta = added.stream().mapToDouble(OfferOption::getPrice).sum() - removed.stream().mapToDouble(OfferOption::getPrice).sum();
        return new OptionsChange(added, removed, Rules.round(delta));
    }

    private Map<String, Object> payload(final OrderRequest r, final Checked c, final OfferSnapshot offer, final OptionsChange optionsChange) {
        final var payload = new LinkedHashMap<String, Object>();
        payload.put("flow", r.getFlow());
        payload.put("customerId", r.getCustomerId());
        payload.put("msisdn", c.msisdn());
        payload.put("cashDue", c.cashDue());
        if (offer != null) {
            payload.put("offer", offer);
        }
        if (optionsChange != null) {
            payload.put("optionsChange", optionsChange);
        }
        payload.put("request", r);
        return payload;
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
