package com.orderfirst.api.stub;

import com.orderfirst.api.domain.Model.Address;
import com.orderfirst.api.domain.Model.Company;
import com.orderfirst.api.domain.Model.Customer;
import com.orderfirst.api.domain.Model.CustomerType;
import com.orderfirst.api.domain.Model.FamilyId;
import com.orderfirst.api.domain.Model.Identity;
import com.orderfirst.api.domain.Model.Line;
import com.orderfirst.api.domain.Model.Offer;
import com.orderfirst.api.domain.Model.OfferOption;
import com.orderfirst.api.domain.Model.OpenMl;
import com.orderfirst.api.domain.Model.PieceType;
import com.orderfirst.api.domain.Model.Promo;
import com.orderfirst.api.domain.Model.Score;
import com.orderfirst.api.domain.Model.SimType;
import com.orderfirst.api.domain.Model.Terminal;
import com.orderfirst.api.port.OrasPort.AddressSuggestion;

import java.util.List;
import java.util.Map;

/** Jeu de démonstration, identique à celui du front (api/mock-data.ts). Montants et libellés d'exemple. */
final class StubData {
    private StubData() {
    }

    static List<Customer> customers() {
        return List.of(
                new Customer("CL-000101", CustomerType.GP,
                        new Identity("Mme", "Dupont", "Marie", "1988-03-14", "971 · Guadeloupe", "Française",
                                PieceType.CNI, "12AB34567", "0690123456", "marie.dupont@exemple.fr"),
                        null,
                        new Address("12 rue des Flamboyants", "97100", "Basse-Terre", "Résidence Les Palmiers, bât. B"),
                        Score.vert,
                        List.of(
                                new Line("0690123456", "bl-60", "Forfait bloqué 60 Go", 22.99, false, SimType.physique, List.of("europe", "multisim")),
                                new Line("0690550123", "nb-100", "Forfait 100 Go", 24.99, false, SimType.esim, List.of()),
                                new Line("0690550124", "pp-classique", "Card prépayée", null, true, SimType.physique, List.of()),
                                new Line("0690550125", "nb-10", "Forfait 10 Go", 9.99, false, SimType.physique, List.of()))),
                new Customer("CL-000102", CustomerType.GP,
                        new Identity("M.", "Martin", "Jean", "1975-07-02", "972 · Martinique", "Française",
                                PieceType.PASSEPORT, "19FR55120", "0696778899", "jean.martin@exemple.fr"),
                        null,
                        new Address("4 avenue des Caraïbes", "97200", "Fort-de-France", ""),
                        Score.orange,
                        List.of(new Line("0696778899", "pp-classique", "Card prépayée", null, true, SimType.physique, List.of()))),
                new Customer("CL-000103", CustomerType.GP,
                        new Identity("M.", "Bernard", "Luc", "1990-11-21", "973 · Guyane", "",
                                PieceType.CNI, "88CD12345", "0694999000", "luc.bernard@exemple.fr"),
                        null,
                        new Address("8 rue Lallouette", "97300", "Cayenne", ""),
                        Score.rouge,
                        List.of(new Line("0694999000", "nb-10", "Forfait 10 Go", 9.99, false, SimType.physique, List.of()))),
                new Customer("CL-000201", CustomerType.PRO,
                        new Identity("Mme", "Lebon", "Sophie", "1982-05-09", "971 · Guadeloupe", "Française",
                                PieceType.CNI, "45GH78901", "0690400500", "contact@boulangerie-des-iles.fr"),
                        new Company("Boulangerie des Îles", "812345678", "81234567800015"),
                        new Address("25 boulevard Légitimus", "97110", "Pointe-à-Pitre", ""),
                        Score.vert,
                        List.of(
                                new Line("0690400500", "nb-200", "Forfait 200 Go 5G", 34.99, false, SimType.physique, List.of("international")),
                                new Line("0690400501", "nb-10", "Forfait 10 Go", 9.99, false, SimType.esim, List.of()))));
    }

    /** Lignes ayant une commande en cours (information portée par api-order). */
    static final List<String> PENDING_ORDER_LINES = List.of("0690550125");

    /** Résultat du re-scoring Préventel pour les clients en base (ligne Card qui change de forfait). */
    static final Map<String, Score> RESCORE = Map.of("CL-000101", Score.vert, "CL-000102", Score.orange);

    private static OfferOption option(final String id, final double price) {
        final var label = switch (id) {
            case "europe" -> "Option Europe 10 Go";
            case "multisim" -> "Multi-SIM";
            case "securite" -> "Sécurité mobile";
            case "international" -> "Appels internationaux";
            default -> id;
        };
        return new OfferOption(id, label, price, false);
    }

    private static OfferOption included(final String id) {
        final var o = option(id, 0);
        return new OfferOption(o.id(), o.label(), 0, true);
    }

    private static Offer offer(final String id, final FamilyId family, final String label, final String detail,
                               final double price, final OpenMl openMl, final OfferOption... options) {
        return new Offer(id, family, label, detail, price, openMl, List.of(options), List.of());
    }

    /** Offres Castor et options proposées avec chacune (prix propres à l'offre, options incluses). */
    static final List<Offer> OFFERS = List.of(
            offer("bl-20", FamilyId.bloque, "Forfait bloqué 20 Go", "Appels 2 h, SMS illimités", 15.99, OpenMl.of(5),
                    option("europe", 5), option("multisim", 3), option("securite", 2)),
            offer("bl-60", FamilyId.bloque, "Forfait bloqué 60 Go", "Appels et SMS illimités", 22.99, OpenMl.of(10),
                    option("europe", 5), option("multisim", 3), option("securite", 2)),
            offer("bl-120", FamilyId.bloque, "Forfait bloqué 120 Go", "Appels et SMS illimités, 5G", 29.99, OpenMl.of(10),
                    included("europe"), option("multisim", 3), option("international", 10)),
            offer("nb-10", FamilyId.nonBloque, "Forfait 10 Go", "Appels et SMS illimités", 9.99, OpenMl.of(5),
                    option("europe", 5), option("securite", 2)),
            offer("nb-100", FamilyId.nonBloque, "Forfait 100 Go", "Appels et SMS illimités, 5G", 24.99, OpenMl.of(10),
                    option("europe", 5), option("multisim", 3), option("securite", 2), option("international", 10)),
            offer("nb-200", FamilyId.nonBloque, "Forfait 200 Go 5G", "Appels illimités, 35 Go en Europe", 34.99, OpenMl.of(10),
                    included("europe"), option("multisim", 3), option("securite", 2), option("international", 8)),
            offer("ie-4g", FamilyId.internet, "Internet Everywhere 4G", "100 Go, routeur nomade", 19.99, OpenMl.of(5),
                    option("securite", 2)),
            offer("home-5g", FamilyId.internet, "4G/5G Home", "Internet illimité à domicile", 34.99, OpenMl.of(10)),
            offer("pp-classique", FamilyId.prepaye, "Carte Prépayée", "Sans engagement, rechargeable", 0, OpenMl.NONE));

    /** Remises api-promo advice, par offre. */
    static final Map<String, List<Promo>> PROMOS = Map.of(
            "bl-60", List.of(new Promo("promo-10e-6m", "−10 € / 6 mois", 10, 6)),
            "bl-120", List.of(new Promo("promo-10e-6m", "−10 € / 6 mois", 10, 6)),
            "nb-100", List.of(new Promo("promo-10e-6m", "−10 € / 6 mois", 10, 6)),
            "home-5g", List.of(new Promo("promo-5e-12m", "−5 € / 12 mois", 5, 12)));

    /** N° sponsor Open ML reconnus par api RHOC. */
    static final List<String> SPONSORS = List.of("0690654321", "0696112233");

    static final List<String> AVAILABLE_NUMBERS = List.of("0690123480", "0690123481", "0690123482", "0690123499", "0691245566");

    static final List<Terminal> TERMINALS = List.of(
            new Terminal("tel-a15", "Samsung Galaxy A15", 149),
            new Terminal("tel-ip15", "iPhone 15 128 Go", 769),
            new Terminal("tel-redmi", "Xiaomi Redmi Note 13", 199));

    /** ICCID en stock boutique (enabler si-financier). */
    static final List<String> ICCID_STOCK = List.of(
            "8933010000000000001", "8933010000000000019", "8933010000000000027",
            "8933010000000000035", "8933010000000000043");

    static final List<AddressSuggestion> ADDRESSES = List.of(
            new AddressSuggestion("12 rue des Flamboyants", "97100", "Basse-Terre"),
            new AddressSuggestion("25 boulevard Légitimus", "97110", "Pointe-à-Pitre"),
            new AddressSuggestion("3 rue Frébault", "97110", "Pointe-à-Pitre"),
            new AddressSuggestion("14 rue Schoelcher", "97100", "Basse-Terre"),
            new AddressSuggestion("4 avenue des Caraïbes", "97200", "Fort-de-France"),
            new AddressSuggestion("8 rue Lallouette", "97300", "Cayenne"),
            new AddressSuggestion("1 place de la Victoire", "97110", "Pointe-à-Pitre"),
            new AddressSuggestion("17 route de Gosier", "97190", "Le Gosier"));
}
