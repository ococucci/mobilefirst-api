package com.orderfirst.api.stub;

import com.orderfirst.api.model.Country;
import com.orderfirst.api.model.Address;
import com.orderfirst.api.model.AddressSuggestion;
import com.orderfirst.api.model.Company;
import com.orderfirst.api.model.Customer;
import com.orderfirst.api.model.CustomerType;
import com.orderfirst.api.model.FamilyId;
import com.orderfirst.api.model.Identity;
import com.orderfirst.api.model.Line;
import com.orderfirst.api.model.Offer;
import com.orderfirst.api.model.OfferLevel;
import com.orderfirst.api.model.OfferOption;
import com.orderfirst.api.model.OpenMl;
import com.orderfirst.api.model.PieceType;
import com.orderfirst.api.model.Promo;
import com.orderfirst.api.model.Score;
import com.orderfirst.api.model.SimType;
import com.orderfirst.api.model.Terminal;
import com.orderfirst.api.model.TerminalColor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Jeu de démonstration, identique à celui du front (api/mock-data.ts). Montants et libellés d'exemple. */
final class StubData {
    private StubData() {
    }

    static List<Customer> customers() {
        return List.of(
                new Customer("CL-000101", CustomerType.GP,
                        new Identity("Mme", "Dupont", "Marie", "1988-03-14", "971 · Guadeloupe", "FR",
                                PieceType.CNI, "12AB34567", "0690123456", "marie.dupont@exemple.fr"),
                        null,
                        new Address("12", "rue des Flamboyants", "97100", "Basse-Terre", "GUADELOUPE", "Résidence Les Palmiers, bât. B"),
                        Score.vert,
                        List.of(
                                new Line("0690123456", "bl-60", "Forfait bloqué 60 Go", 22.99, false, SimType.physique, List.of("europe", "multisim")),
                                new Line("0690550123", "nb-100", "Forfait 100 Go", 24.99, false, SimType.esim, List.of()),
                                new Line("0690550124", "pp-classique", "Card prépayée", null, true, SimType.physique, List.of()),
                                new Line("0690550125", "nb-10", "Forfait 10 Go", 9.99, false, SimType.physique, List.of()))),
                new Customer("CL-000102", CustomerType.GP,
                        new Identity("M.", "Martin", "Jean", "1975-07-02", "972 · Martinique", "FR",
                                PieceType.PASSEPORT, "19FR55120", "0696778899", "jean.martin@exemple.fr"),
                        null,
                        new Address("4", "avenue des Caraïbes", "97200", "Fort-de-France", "MARTINIQUE", ""),
                        Score.orange,
                        List.of(new Line("0696778899", "pp-classique", "Card prépayée", null, true, SimType.physique, List.of()))),
                new Customer("CL-000103", CustomerType.GP,
                        new Identity("M.", "Bernard", "Luc", "1990-11-21", "973 · Guyane", "",
                                PieceType.CNI, "88CD12345", "0694999000", "luc.bernard@exemple.fr"),
                        null,
                        new Address("8", "rue Lallouette", "97300", "Cayenne", "GUYANE FRANCAISE", ""),
                        Score.rouge,
                        List.of(new Line("0694999000", "nb-10", "Forfait 10 Go", 9.99, false, SimType.physique, List.of()))),
                new Customer("CL-000201", CustomerType.PRO,
                        new Identity("Mme", "Lebon", "Sophie", "1982-05-09", "971 · Guadeloupe", "FR",
                                PieceType.CNI, "45GH78901", "0690400500", "contact@boulangerie-des-iles.fr"),
                        new Company("Boulangerie des Îles", "812345678", "81234567800015"),
                        new Address("25", "boulevard Légitimus", "97110", "Pointe-à-Pitre", "GUADELOUPE", ""),
                        Score.vert,
                        List.of(
                                new Line("0690400500", "nb-200", "Forfait 200 Go 5G", 34.99, false, SimType.physique, List.of("international")),
                                new Line("0690400501", "nb-10", "Forfait 10 Go", 9.99, false, SimType.esim, List.of()))));
    }

    /** Lignes ayant une commande en cours (information portée par api-order). */
    static final List<String> PENDING_ORDER_LINES = List.of("0690550125");

    /** Résultat du re-scoring Préventel pour les clients en base (ligne Card qui change de forfait). */
    static final Map<String, Score> RESCORE = Map.of("CL-000101", Score.vert, "CL-000102", Score.orange);

    /**
     * Options incompatibles. Chaque incompatibilité n'est déclarée que d'un côté, comme le catalogue peut le faire :
     * elle s'applique quand même dans les deux sens.
     */
    private static final Map<String, List<String>> INCOMPATIBLE = Map.of(
            "europe-plus", List.of("europe"),
            "montre", List.of("multisim"));

    private static OfferOption option(final String id, final double price) {
        final var label = switch (id) {
            case "europe" -> "Option Europe 10 Go";
            case "europe-plus" -> "Option Europe 25 Go";
            case "multisim" -> "Multi-SIM";
            case "montre" -> "eSIM montre connectée";
            case "securite" -> "Sécurité mobile";
            case "international" -> "Appels internationaux";
            default -> id;
        };
        return new OfferOption(id, label, price, false, INCOMPATIBLE.getOrDefault(id, List.of()));
    }

    private static OfferOption included(final String id) {
        final var o = option(id, 0);
        return new OfferOption(o.getId(), o.getLabel(), 0, true, o.getIncompatibleOptions());
    }

    /** Remise Open ML décrite comme une promo : code et libellé. */
    private static OpenMl openMl(final int monthlyDiscount) {
        return OpenMl.of("OPENML_" + monthlyDiscount + "E", "Open ML −" + monthlyDiscount + " €/mois", monthlyDiscount);
    }

    private static Offer offer(final String id, final FamilyId family, final OfferLevel level, final String label, final String detail,
                               final double price, final OpenMl openMl, final OfferOption... options) {
        return new Offer(id, family, level, label, detail, price, openMl, List.of(options), null);
    }

    /** Offres Castor et options proposées avec chacune (prix propres à l'offre, options incluses). */
    static final List<Offer> OFFERS = List.of(
            offer("bl-20", FamilyId.bloque, OfferLevel.nonsub, "Forfait bloqué 20 Go", "Appels 2 h, SMS illimités", 15.99, openMl(5),
                    option("europe", 5), option("multisim", 3), option("securite", 2)),
            offer("bl-60", FamilyId.bloque, OfferLevel.silver, "Forfait bloqué 60 Go", "Appels et SMS illimités", 22.99, openMl(10),
                    option("europe", 5), option("europe-plus", 9), option("multisim", 3), option("montre", 5), option("securite", 2)),
            offer("bl-120", FamilyId.bloque, OfferLevel.gold, "Forfait bloqué 120 Go", "Appels et SMS illimités, 5G", 29.99, openMl(10),
                    included("europe"), option("europe-plus", 7), option("multisim", 3), option("montre", 5), option("international", 10)),
            offer("nb-10", FamilyId.nonBloque, OfferLevel.nonsub, "Forfait 10 Go", "Appels et SMS illimités", 9.99, openMl(5),
                    option("europe", 5), option("securite", 2)),
            offer("nb-100", FamilyId.nonBloque, OfferLevel.elite, "Forfait 100 Go", "Appels et SMS illimités, 5G", 24.99, openMl(10),
                    option("europe", 5), option("europe-plus", 9), option("multisim", 3), option("montre", 5), option("securite", 2),
                    option("international", 10)),
            offer("nb-200", FamilyId.nonBloque, OfferLevel.platine, "Forfait 200 Go 5G", "Appels illimités, 35 Go en Europe", 34.99, openMl(10),
                    included("europe"), option("multisim", 3), option("securite", 2), option("international", 8)),
            offer("ie-4g", FamilyId.internet, OfferLevel.nonsub, "Internet Everywhere 4G", "100 Go, routeur nomade", 19.99, openMl(5),
                    option("securite", 2)),
            offer("home-5g", FamilyId.internet, OfferLevel.silver, "4G/5G Home", "Internet illimité à domicile", 34.99, openMl(10)),
            offer("pp-classique", FamilyId.prepaye, OfferLevel.card, "Carte Prépayée", "Sans engagement, rechargeable", 10, OpenMl.none()));

    /** Remise api-promo advice de chaque offre éligible (une seule par offre). */
    static final Map<String, Promo> PROMOS = Map.of(
            "bl-60", new Promo("promo-10e-6m", "−10 € / 6 mois", 10, 6),
            "bl-120", new Promo("promo-10e-6m", "−10 € / 6 mois", 10, 6),
            "nb-100", new Promo("promo-10e-6m", "−10 € / 6 mois", 10, 6),
            "home-5g", new Promo("promo-5e-12m", "−5 € / 12 mois", 5, 12));

    /** Pays du référentiel (identifiant ISO 3166-1 alpha-2, libellé). */
    static final List<Country> COUNTRIES = List.of(
            new Country("FR", "France"), new Country("DZ", "Algérie"), new Country("DE", "Allemagne"),
            new Country("AG", "Antigua-et-Barbuda"), new Country("AR", "Argentine"), new Country("BS", "Bahamas"),
            new Country("BB", "Barbade"), new Country("BE", "Belgique"), new Country("BJ", "Bénin"),
            new Country("BR", "Brésil"), new Country("CM", "Cameroun"), new Country("CA", "Canada"),
            new Country("CN", "Chine"), new Country("CO", "Colombie"), new Country("CG", "Congo"),
            new Country("CD", "Congo (Rép. dém.)"), new Country("CI", "Côte d'Ivoire"), new Country("CU", "Cuba"),
            new Country("DM", "Dominique"), new Country("EG", "Égypte"), new Country("ES", "Espagne"),
            new Country("US", "États-Unis"), new Country("GA", "Gabon"), new Country("GB", "Royaume-Uni"),
            new Country("GD", "Grenade"), new Country("GN", "Guinée"), new Country("GY", "Guyana"),
            new Country("HT", "Haïti"), new Country("IN", "Inde"), new Country("IT", "Italie"),
            new Country("JM", "Jamaïque"), new Country("LB", "Liban"), new Country("LU", "Luxembourg"),
            new Country("MG", "Madagascar"), new Country("ML", "Mali"), new Country("MA", "Maroc"),
            new Country("MU", "Maurice"), new Country("MX", "Mexique"), new Country("NL", "Pays-Bas"),
            new Country("PE", "Pérou"), new Country("PL", "Pologne"), new Country("PT", "Portugal"),
            new Country("DO", "République dominicaine"), new Country("RO", "Roumanie"), new Country("KN", "Saint-Christophe-et-Niévès"),
            new Country("LC", "Sainte-Lucie"), new Country("VC", "Saint-Vincent-et-les-Grenadines"), new Country("SN", "Sénégal"),
            new Country("CH", "Suisse"), new Country("SR", "Suriname"), new Country("SY", "Syrie"),
            new Country("TT", "Trinité-et-Tobago"), new Country("TN", "Tunisie"), new Country("TR", "Turquie"),
            new Country("UA", "Ukraine"), new Country("VE", "Venezuela"), new Country("VN", "Viêt Nam"));

    /** Sponsors Open ML reconnus par api RHOC (N° de mobile ou ND -> nom, prénom). */
    static final Map<String, List<String>> SPONSORS = Map.of(
            "0690654321", List.of("MARTIN", "JULIEN"),
            "0696112233", List.of("LOUISON", "CHANTAL"),
            "0590240254", List.of("ROSEMOND", "PATRICE"));

    static final List<String> AVAILABLE_NUMBERS = List.of("0690123480", "0690123481", "0690123482", "0690123499", "0691245566");

    /** Absence de tarif pour un niveau : le terminal n'est pas proposé avec les offres de cette gamme. */
    private static final double NONE = Double.NaN;

    /** Prix d'un terminal pour chaque niveau d'offre, dans l'ordre : nonsub, card, silver, gold, elite, platine. */
    private static Map<OfferLevel, Double> prices(final double... byLevel) {
        final var levels = OfferLevel.values();
        final var prices = new EnumMap<OfferLevel, Double>(OfferLevel.class);
        for (int i = 0; i < levels.length; i++) {
            if (!Double.isNaN(byLevel[i])) {
                prices.put(levels[i], byLevel[i]);
            }
        }
        return Collections.unmodifiableMap(prices);
    }

    private static Terminal terminal(final String id, final String brand, final String model, final Map<OfferLevel, Double> prices,
                                     final List<String> colors) {
        return new Terminal(id, brand, model, prices, colors.stream().map(c -> new TerminalColor(id + "-" + c, c)).toList());
    }

    private static final List<String> APPLE_COLORS = List.of("blanc", "bleu", "noir", "rose", "vert");

    /** Terminaux (enabler si-financier) : prix selon le niveau de l'offre et couleurs proposées. */
    static final List<Terminal> TERMINALS = List.of(
            terminal("again-a1", "AGAIN", "Again A1 4G", prices(49, 49, 29, 9, 5, 1), List.of("noir")),
            terminal("alcatel-1b", "ALCATEL", "Alcatel 1B 32Go", prices(79, 79, 59, 29, 19, 9), List.of("bleu", "noir")),
            terminal("alcatel-3l", "ALCATEL", "Alcatel 3L 64Go", prices(129, 129, 99, 59, 44, 29), List.of("noir")),
            terminal("ip15-128", "APPLE", "iPhone 15 128Go", prices(769, 769, 649, 529, 464, 399), APPLE_COLORS),
            terminal("ip15-256", "APPLE", "iPhone 15 256Go", prices(899, 899, 779, 659, 594, 529), APPLE_COLORS),
            terminal("ip15plus-256", "APPLE", "iPhone 15 PLUS 256Go", prices(999, 999, 879, 759, 694, 629), APPLE_COLORS),
            terminal("ip15pro-128", "APPLE", "iPhone 15 PRO 128Go", prices(NONE, NONE, NONE, 869, 804, 739), List.of("blanc", "bleu", "noir")),
            terminal("ip15pro-256", "APPLE", "iPhone 15 PRO 256Go", prices(NONE, NONE, NONE, 999, 934, 869), List.of("blanc", "bleu", "noir")),
            terminal("ip15promax-256", "APPLE", "iPhone 15 PRO MAX 256Go", prices(NONE, NONE, NONE, 1119, 1054, 989), List.of("blanc", "bleu", "noir")),
            terminal("ip15promax-512", "APPLE", "iPhone 15 PRO MAX 512Go", prices(NONE, NONE, NONE, 1369, 1304, 1239), List.of("blanc", "bleu", "noir")),
            terminal("ip16-128", "APPLE", "iPhone 16 5G 128Go", prices(969, 969, 829, 689, 619, 549), APPLE_COLORS),
            terminal("ip16-256", "APPLE", "iPhone 16 5G 256Go", prices(1099, 1099, 959, 819, 749, 679), APPLE_COLORS),
            terminal("ip16plus-128", "APPLE", "iPhone 16 PLUS 5G 128Go", prices(1119, 1119, 979, 839, 769, 699), APPLE_COLORS),
            terminal("ip16plus-256", "APPLE", "iPhone 16 PLUS 5G 256Go", prices(1249, 1249, 1109, 969, 899, 829), APPLE_COLORS),
            terminal("bb-key2", "BLACK BERRY", "BlackBerry KEY2 64Go", prices(349, 349, 299, 249, 224, 199), List.of("noir")),
            terminal("cadaoz-c3", "CADAOZ", "Cadaoz C3 4G", prices(59, 59, 39, 19, 10, 1), List.of("blanc", "noir")),
            terminal("sam-a15", "SAMSUNG", "Galaxy A15 128Go", prices(149, 149, 99, 49, 25, 1), List.of("blanc", "bleu", "noir")),
            terminal("sam-s24", "SAMSUNG", "Galaxy S24 5G 256Go", prices(NONE, NONE, 749, 599, 524, 449), List.of("bleu", "noir", "vert")),
            terminal("xiaomi-rn13", "XIAOMI", "Redmi Note 13 128Go", prices(199, 199, 149, 99, 74, 49), List.of("bleu", "noir", "vert")));

    /** ICCID en stock boutique (enabler si-financier). */
    static final List<String> ICCID_STOCK = List.of(
            "8933010000000000001", "8933010000000000019", "8933010000000000027",
            "8933010000000000035", "8933010000000000043");

    static final List<AddressSuggestion> ADDRESSES = List.of(
            new AddressSuggestion("12", "rue des Flamboyants", "97100", "Basse-Terre"),
            new AddressSuggestion("25", "boulevard Légitimus", "97110", "Pointe-à-Pitre"),
            new AddressSuggestion("3", "rue Frébault", "97110", "Pointe-à-Pitre"),
            new AddressSuggestion("14", "rue Schoelcher", "97100", "Basse-Terre"),
            new AddressSuggestion("4", "avenue des Caraïbes", "97200", "Fort-de-France"),
            new AddressSuggestion("8", "rue Lallouette", "97300", "Cayenne"),
            new AddressSuggestion("1", "place de la Victoire", "97110", "Pointe-à-Pitre"),
            new AddressSuggestion("17", "route de Gosier", "97190", "Le Gosier"));
}
