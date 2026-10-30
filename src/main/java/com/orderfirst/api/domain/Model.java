package com.orderfirst.api.domain;

import java.util.List;

/** Modèle métier de la prise de commande mobile, aligné sur le front (domain/models.ts). */
public final class Model {
    private Model() {
    }

    public enum Score { vert, orange, rouge }

    public enum Flow { acquisition, forfait, options, sim }

    public enum FamilyId {
        bloque("bloque"), nonBloque("non-bloque"), internet("internet"), prepaye("prepaye");

        private final String code;

        FamilyId(final String code) {
            this.code = code;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String code() {
            return code;
        }

        @com.fasterxml.jackson.annotation.JsonCreator
        public static FamilyId of(final String code) {
            for (final var f : values()) {
                if (f.code.equals(code)) {
                    return f;
                }
            }
            throw new IllegalArgumentException("Famille inconnue : " + code);
        }
    }

    public enum SimType { physique, esim }

    public enum PieceType { CNI, PASSEPORT }

    public enum CustomerType { GP, PRO }

    public record Identity(
            String civilite, String nom, String prenom, String dateNaissance, String departementNaissance,
            String nationalite, PieceType pieceType, String pieceNumero, String mobileContact, String email) {
    }

    public record Address(String ligne, String codePostal, String commune, String complement) {
    }

    public record Company(String raisonSociale, String siren, String siret) {
    }

    /** {@code monthlyPrice} est null pour une ligne Card prépayée (sans mensualité). */
    public record Line(String msisdn, String offerId, String offerLabel, Double monthlyPrice, boolean isCard,
                       SimType simType, List<String> options) {
    }

    public record Customer(String id, CustomerType type, Identity identity, Company company, Address address,
                           Score score, List<Line> lines) {
        public Customer withData(final Identity identity, final Address address, final Score score) {
            return new Customer(id, type, identity, company, address, score, lines);
        }
    }

    /**
     * Offre du catalogue avec ce qui en dépend : éligibilité Open ML, options proposées (prix propres à l'offre,
     * éventuellement incluses) et remise appliquée automatiquement (une seule par offre, null s'il n'y en a pas).
     */
    public record Offer(String id, FamilyId family, String label, String detail, double price, OpenMl openMl,
                        List<OfferOption> options, Promo promo) {
        public java.util.Optional<OfferOption> option(final String optionId) {
            return options.stream().filter(o -> o.id().equals(optionId)).findFirst();
        }

        public Offer withPromo(final Promo promo) {
            return new Offer(id, family, label, detail, price, openMl, options, promo);
        }
    }

    /** Remise Open ML : −5 € par mois pour une offre d'entrée de gamme, −10 € sinon ; null si non éligible. */
    public record OpenMl(boolean eligible, Double monthlyDiscount) {
        public static final OpenMl NONE = new OpenMl(false, null);

        public static OpenMl of(final double monthlyDiscount) {
            return new OpenMl(true, monthlyDiscount);
        }
    }

    /** Option proposée avec une offre ; une option incluse est gratuite et toujours active. */
    public record OfferOption(String id, String label, double price, boolean included) {
    }

    /** Remise commerciale appliquée automatiquement à l'offre (api-promo advice) ; les remises ne se cumulent pas. */
    public record Promo(String id, String label, double monthlyDiscount, int durationMonths) {
    }

    public record Terminal(String id, String label, double price) {
    }

    public record SimReason(String id, String label, boolean fee) {
    }

    public static final List<SimReason> SIM_REASONS = List.of(
            new SimReason("mobile", "Changement de mobile", false),
            new SimReason("technique", "Problème technique", false),
            new SimReason("obsolete", "SIM obsolète", false),
            new SimReason("perte-vol", "SIM perdue ou volée", true),
            new SimReason("endommagee", "SIM endommagée", false),
            new SimReason("esim", "Passage à l'eSIM", false),
            new SimReason("autre", "Autre", false));

    public static final List<String> DONOR_OPERATORS = List.of("SFR Caraibes", "FREE", "Dauphin Telecom", "UTS", "DIGICEL");

    public static final double CARD_SIM_PRICE = 10;
    public static final double SIM_LOSS_FEE = 10;
}
