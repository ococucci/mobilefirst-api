package com.orderfirst.api.service;

import com.orderfirst.api.domain.ApiException;
import com.orderfirst.api.domain.Model;
import com.orderfirst.api.domain.Model.Customer;
import com.orderfirst.api.domain.Model.CustomerType;
import com.orderfirst.api.domain.Model.FamilyId;
import com.orderfirst.api.domain.Model.Flow;
import com.orderfirst.api.domain.Model.Line;
import com.orderfirst.api.domain.Model.OfferOption;
import com.orderfirst.api.domain.Model.Promo;
import com.orderfirst.api.domain.Model.Offer;
import com.orderfirst.api.domain.Model.Score;
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
import com.orderfirst.api.service.OrderRequest.NumberChoice;
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

    public record CreatedOrder(String orderNumber, Instant createdAt, double cashDue) {
    }

    /**
     * Message publié dans le bus PubSub à la validation de la commande. L'offre vendue (acquisition, changement de
     * forfait) ou les options modifiées y sont recopiées avec libellés et prix du jour de la vente.
     */
    public record OrderValidated(String topic, String orderNumber, Instant createdAt, Flow flow, String customerId,
                                 String msisdn, double cashDue, OfferSnapshot offer, OptionsChange optionsChange,
                                 OrderRequest order) {
    }

    /** Offre vendue : options actives (incluses comprises), remises appliquées, Open ML et mensualités calculées. */
    public record OfferSnapshot(String id, String label, double price, List<OfferOption> options, List<Promo> promos,
                                OpenMlApplied openMl, double monthlyPrice, Double monthlyPriceDuringPromo) {
    }

    public record OpenMlApplied(String sponsor, double monthlyDiscount) {
    }

    /** Modification d'options : options ajoutées et retirées, et variation de la mensualité. */
    public record OptionsChange(List<OfferOption> added, List<OfferOption> removed, double monthlyDelta) {
    }

    public CreatedOrder create(final OrderRequest request) {
        final var checked = validate(request);
        final var offer = request.flow() == Flow.acquisition || request.flow() == Flow.forfait ? snapshot(request, checked) : null;
        final var optionsChange = request.flow() == Flow.options ? optionsChange(request, checked) : null;
        final var created = orders.create(checked.msisdn(), payload(request, checked, offer, optionsChange));
        if (request.flow() == Flow.acquisition && request.number().mode() == NumberChoice.Mode.nouveau) {
            ressource.reserve(request.number().msisdn());
        }
        if (request.sim() != null) {
            siFinancier.consumeIccid(request.sim().iccid());
        }
        cashDueByOrder.put(created.orderNumber(), checked.cashDue());
        bus.publish(new OrderValidated(OrderEventPublisher.TOPIC, created.orderNumber(), created.createdAt(), request.flow(),
                request.customerId(), checked.msisdn(), checked.cashDue(), offer, optionsChange, request));
        return new CreatedOrder(created.orderNumber(), created.createdAt(), checked.cashDue());
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
        if (r == null || r.flow() == null) {
            new Validation().check(true, "flow", "La demande est obligatoire.").throwIfInvalid();
        }
        if (isBlank(r.customerId())) {
            new Validation().check(true, "customerId", "Le client est obligatoire.").throwIfInvalid();
        }
        final var customer = customers.byId(r.customerId());
        if (customer.score() == Score.rouge) {
            throw ApiException.rule("Score Préventel rouge : aucune vente possible.");
        }
        final var v = new Validation();
        final var checked = switch (r.flow()) {
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
        if (customer.type() == CustomerType.PRO) {
            throw ApiException.rule("Client entreprise : seuls les actes de gestion sur un client existant sont autorisés.");
        }
        v.identity("holder", r.holder(), true, today()).address("billingAddress", r.billingAddress());
        number(r.number(), v);
        final var offer = offer(r.offerId(), customer.score(), v);
        checkOptions(r, offer, v);
        openMl(r, offer, null, v);
        checkSim(r, v);
        double terminalPrice = 0;
        final var isCard = offer != null && offer.family() == FamilyId.prepaye;
        if (r.terminal() != null && !isCard) {
            final var terminal = siFinancier.terminals().stream().filter(t -> t.id().equals(r.terminal().id())).findFirst();
            v.check(terminal.isEmpty(), "terminal.id", "Choisissez un terminal.");
            if (isBlank(r.terminal().imei())) {
                v.check(true, "terminal.imei", "L'IMEI est obligatoire.");
            } else {
                v.check(!Rules.isValidImei(r.terminal().imei()), "terminal.imei", "IMEI invalide : 15 chiffres.");
            }
            terminalPrice = terminal.map(Model.Terminal::price).orElse(0.);
        }
        if (!isCard) {
            v.check(!iml.exists(r.sepaMandateRef()), "sepaMandateRef", "Le mandat SEPA doit être enregistré (POST /sepa-mandates).");
        }
        final var msisdn = r.number() == null ? "" : Rules.digits(
                r.number().mode() == NumberChoice.Mode.portabilite ? r.number().portedMsisdn() : r.number().msisdn());
        return new Checked(msisdn, Rules.acquisitionCashDue(offer, terminalPrice), customer, offer, null);
    }

    private Checked forfait(final OrderRequest r, final Customer customer, final Validation v) {
        final var line = modifiableLine(r, customer, v);
        var score = customer.score();
        if (line != null && line.isCard()) {
            score = scoring.rescore(customer);
            if (score != Score.vert) {
                throw ApiException.rule("Re-scoring Préventel " + score + " : aucun forfait ne peut être pris pour cette ligne Card.");
            }
        }
        final var offer = offer(r.offerId(), score, v);
        v.check(offer != null && line != null && offer.id().equals(line.offerId()), "offerId", "Choisissez une offre différente de l'offre actuelle.");
        checkOptions(r, offer, v);
        openMl(r, offer, line, v);
        return new Checked(line == null ? "" : line.msisdn(), 0, customer, offer, line);
    }

    private Checked options(final OrderRequest r, final Customer customer, final Validation v) {
        final var line = modifiableLine(r, customer, v);
        // Les options proposées sont celles de l'offre actuelle de la ligne.
        final var offer = line == null ? null : catalog.offer(line.offerId()).orElse(null);
        checkOptions(r, offer, v);
        if (offer != null && !v.has("options")) {
            v.check(Objects.equals(paidOptions(offer, line.options()), paidOptions(offer, r.optionsOrEmpty())), "options",
                    "Ajoutez ou retirez au moins une option.");
        }
        return new Checked(line == null ? "" : line.msisdn(), 0, customer, offer, line);
    }

    /** Options payantes choisies : une option incluse ne compte pas comme un ajout ou un retrait. */
    private static Set<String> paidOptions(final Offer offer, final List<String> ids) {
        return Rules.selectedOptions(offer, ids).stream().filter(o -> !o.included()).map(OfferOption::id)
                .collect(java.util.stream.Collectors.toSet());
    }

    private Checked sim(final OrderRequest r, final Customer customer, final Validation v) {
        final var line = modifiableLine(r, customer, v);
        v.check(Model.SIM_REASONS.stream().noneMatch(reason -> reason.id().equals(r.simReason())), "simReason",
                "Choisissez la raison du changement.");
        checkSim(r, v);
        return new Checked(line == null ? "" : line.msisdn(), Rules.simChangeFee(r.simReason()), customer, null, line);
    }

    // ----- Contrôles communs

    private Line modifiableLine(final OrderRequest r, final Customer customer, final Validation v) {
        if (isBlank(r.lineMsisdn())) {
            v.check(true, "lineMsisdn", "Choisissez la ligne concernée.");
            return null;
        }
        final var line = CustomerService.line(customer, r.lineMsisdn());
        if (!orders.linesWithPendingOrder(List.of(line.msisdn())).isEmpty()) {
            throw ApiException.rule("La ligne " + line.msisdn() + " a une commande en cours : elle ne peut pas être modifiée.");
        }
        return line;
    }

    private void number(final NumberChoice n, final Validation v) {
        if (n == null || n.mode() == null) {
            v.check(true, "number.mode", "Choisissez le numéro d'appel.");
            return;
        }
        if (n.mode() == NumberChoice.Mode.nouveau) {
            if (isBlank(n.msisdn())) {
                v.check(true, "number.msisdn", "Choisissez un numéro dans la liste.");
            } else {
                v.check(!ressource.availableNumbers().contains(Rules.digits(n.msisdn())), "number.msisdn", "Ce numéro n'est plus disponible.");
            }
            return;
        }
        if (isBlank(n.rio())) {
            v.check(true, "number.rio", "Le code RIO est obligatoire.");
        } else {
            v.check(!Rules.isValidRio(n.rio()), "number.rio", "Code RIO invalide : 12 caractères (ex. 01P123456ABC).");
        }
        if (isBlank(n.portedMsisdn())) {
            v.check(true, "number.portedMsisdn", "Le numéro à conserver est obligatoire.");
        } else {
            v.check(!Rules.isValidMsisdn(n.portedMsisdn()), "number.portedMsisdn", Validation.MSISDN_MESSAGE);
        }
        v.check(n.operator() == null || !Model.DONOR_OPERATORS.contains(n.operator()), "number.operator", "L'opérateur donneur est obligatoire.");
        if (isBlank(n.portingDate())) {
            v.check(true, "number.portingDate", "La date d'exécution du portage est obligatoire.");
        } else {
            final var date = Validation.parseDate(n.portingDate());
            v.check(date == null || date.isBefore(today()), "number.portingDate", "La date ne peut pas être dans le passé.");
        }
    }

    private Offer offer(final String offerId, final Score score, final Validation v) {
        final var offer = offerId == null ? null : catalog.offer(offerId).orElse(null);
        if (offer == null) {
            v.check(true, "offerId", "Choisissez un forfait.");
            return null;
        }
        v.check(!Rules.allowedFamilies(score).contains(offer.family()), "offerId",
                "Score " + score + " : seule l'offre Carte Prépayée est vendable.");
        return offer;
    }

    private void checkOptions(final OrderRequest r, final Offer offer, final Validation v) {
        if (offer == null) {
            return;
        }
        v.check(offer.options().isEmpty() && !r.optionsOrEmpty().isEmpty(), "options", "Aucune option n'est proposée avec cette offre.");
        v.check(r.optionsOrEmpty().stream().anyMatch(o -> o == null || offer.option(o).isEmpty()), "options",
                "Option non proposée avec cette offre.");
    }

    private void openMl(final OrderRequest r, final Offer offer, final Line line, final Validation v) {
        if (isBlank(r.openMlSponsor())) {
            return;
        }
        if (!Rules.isOpenMlAvailable(offer, line)) {
            v.check(offer != null, "openMlSponsor", "Remise Open ML indisponible pour la Carte Prépayée et pour une ligne Card.");
            return;
        }
        v.check(!rhoc.isValidSponsor(r.openMlSponsor()), "openMlSponsor", "Sponsor non reconnu par api RHOC.");
    }

    private void checkSim(final OrderRequest r, final Validation v) {
        if (r.sim() == null || isBlank(r.sim().iccid())) {
            v.check(true, "sim.iccid", "La SIM est obligatoire : saisissez ou scannez l'ICCID.");
            return;
        }
        v.check(r.sim().type() == null, "sim.type", "Choisissez le type de SIM.");
        switch (siFinancier.checkIccid(r.sim().iccid())) {
            case format -> v.check(true, "sim.iccid", "ICCID invalide : 19 ou 20 chiffres commençant par 89.");
            case inconnu -> v.check(true, "sim.iccid", "ICCID inconnu du stock boutique (si-financier).");
            case IccidCheck.ok -> {
            }
        }
    }

    private void confirmation(final OrderRequest r, final Validation v) {
        if (Rules.requiresContract(r.flow())) {
            v.check(!documents.exists(r.contractRef()), "contractRef", "Le contrat doit être généré avant la validation.");
            v.check(!Boolean.TRUE.equals(r.contractSigned()), "contractSigned", "Cochez « Le client a signé le contrat » pour valider.");
        }
        v.check(!Boolean.TRUE.equals(r.cgvAccepted()), "cgvAccepted", "Les conditions générales de vente doivent être acceptées.");
        v.check(!Boolean.TRUE.equals(r.privacyAccepted()), "privacyAccepted", "La politique de données personnelles doit être acceptée.");
    }

    private OfferSnapshot snapshot(final OrderRequest r, final Checked c) {
        final var offer = c.offer();
        final boolean openMl = !isBlank(r.openMlSponsor()) && Rules.isOpenMlAvailable(offer, c.line());
        final double monthly = Rules.monthly(offer, r.optionsOrEmpty(), openMl);
        return new OfferSnapshot(offer.id(), offer.label(), offer.price(), Rules.selectedOptions(offer, r.optionsOrEmpty()),
                offer.promos(), openMl ? new OpenMlApplied(Rules.digits(r.openMlSponsor()), Rules.openMlDiscount(offer)) : null,
                monthly, Rules.monthlyDuringPromo(offer, monthly));
    }

    private OptionsChange optionsChange(final OrderRequest r, final Checked c) {
        final var current = paidOptions(c.offer(), c.line().options());
        final var wanted = paidOptions(c.offer(), r.optionsOrEmpty());
        final var added = c.offer().options().stream().filter(o -> wanted.contains(o.id()) && !current.contains(o.id())).toList();
        final var removed = c.offer().options().stream().filter(o -> current.contains(o.id()) && !wanted.contains(o.id())).toList();
        final double delta = added.stream().mapToDouble(OfferOption::price).sum() - removed.stream().mapToDouble(OfferOption::price).sum();
        return new OptionsChange(added, removed, Rules.round(delta));
    }

    private Map<String, Object> payload(final OrderRequest r, final Checked c, final OfferSnapshot offer, final OptionsChange optionsChange) {
        final var payload = new LinkedHashMap<String, Object>();
        payload.put("flow", r.flow());
        payload.put("customerId", r.customerId());
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
