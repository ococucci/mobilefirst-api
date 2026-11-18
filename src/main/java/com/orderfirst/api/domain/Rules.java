package com.orderfirst.api.domain;

import com.orderfirst.api.model.Address;
import com.orderfirst.api.model.FamilyId;
import com.orderfirst.api.model.Flow;
import com.orderfirst.api.model.Identity;
import com.orderfirst.api.model.Line;
import com.orderfirst.api.model.Offer;
import com.orderfirst.api.model.OfferOption;
import com.orderfirst.api.model.Score;
import com.orderfirst.api.model.Terminal;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Règles de gestion et contrôles de saisie (section 2 de la cinématique), identiques à ceux du front. */
public final class Rules {
    private static final Pattern MSISDN = Pattern.compile("^0[67]\\d{8}$");
    private static final Pattern RIO = Pattern.compile("^\\d{2}[PE][A-Z0-9]{6}[A-Z0-9]{3}$");
    private static final Pattern IBAN = Pattern.compile("^[A-Z]{2}\\d{2}[A-Z0-9]{10,30}$");
    private static final Pattern ICCID = Pattern.compile("^89\\d{17,18}$");
    private static final Pattern IMEI = Pattern.compile("^\\d{15}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PIECE = Pattern.compile("^[A-Z0-9]{9}$|^[A-Z0-9]{12}$");

    private Rules() {
    }

    public static String digits(final String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public static String compact(final String value) {
        return value == null ? "" : value.replaceAll("[\\s.-]", "").toUpperCase(Locale.ROOT);
    }

    public static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    public static boolean isValidMsisdn(final String value) {
        return value != null && MSISDN.matcher(value.replaceAll("[\\s.]", "")).matches();
    }

    public static boolean isValidRio(final String value) {
        return RIO.matcher(compact(value)).matches();
    }

    /** Contrôle ISO 13616 (modulo 97). */
    public static boolean isValidIban(final String value) {
        final var iban = compact(value);
        if (!IBAN.matcher(iban).matches()) {
            return false;
        }
        final var rearranged = iban.substring(4) + iban.substring(0, 4);
        final var numeric = new StringBuilder();
        for (final char c : rearranged.toCharArray()) {
            numeric.append(Character.isLetter(c) ? String.valueOf(c - 55) : String.valueOf(c));
        }
        return new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    public static boolean isValidIccid(final String value) {
        return ICCID.matcher(compact(value)).matches();
    }

    public static boolean isValidImei(final String value) {
        final var imei = compact(value);
        return IMEI.matcher(imei).matches() && luhn(imei);
    }

    public static boolean isValidEmail(final String value) {
        return value != null && EMAIL.matcher(value.trim()).matches();
    }

    /** Ancienne CNI : 12 caractères ; nouvelle CNI et passeport : 9 caractères. */
    public static boolean isValidPieceNumber(final String value) {
        return PIECE.matcher(compact(value)).matches();
    }

    static boolean luhn(final String value) {
        final var d = digits(value);
        int sum = 0;
        for (int i = 0; i < d.length(); i++) {
            int n = d.charAt(d.length() - 1 - i) - '0';
            if (i % 2 == 1) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
        }
        return !d.isEmpty() && sum % 10 == 0;
    }

    /** Score rouge : aucune vente. Orange : Carte Prépayée uniquement. Vert : parcours complet. */
    public static List<FamilyId> allowedFamilies(final Score score) {
        return switch (score) {
            case vert -> List.of(FamilyId.values());
            case orange -> List.of(FamilyId.prepaye);
            case rouge -> List.of();
        };
    }

    /** Seuls le nom, le prénom et la date de naissance relancent le contrôle Préventel. */
    public static boolean identityChangeRequiresRescore(final Identity before, final Identity after) {
        return !norm(before.getNom()).equals(norm(after.getNom()))
                || !norm(before.getPrenom()).equals(norm(after.getPrenom()))
                || !Objects.equals(before.getDateNaissance(), after.getDateNaissance());
    }

    /** Open ML : selon l'éligibilité de l'offre (pas la Carte Prépayée) et jamais pour une ligne Card. */
    public static boolean isOpenMlAvailable(final Offer offer, final Line line) {
        return offer != null && offer.getOpenMl().isEligible() && (line == null || !line.isCard());
    }

    /** Montant mensuel de la remise Open ML de l'offre (5 € pour une offre d'entrée de gamme, 10 € sinon). */
    public static double openMlDiscount(final Offer offer) {
        return offer.getOpenMl().getMonthlyDiscount() == null ? 0 : offer.getOpenMl().getMonthlyDiscount();
    }

    /** Options actives avec l'offre : les options incluses, plus celles choisies parmi celles proposées. */
    public static List<OfferOption> selectedOptions(final Offer offer, final List<String> optionIds) {
        return offer.getOptions().stream().filter(o -> o.isIncluded() || optionIds.contains(o.getId())).toList();
    }

    /** Première paire d'options actives incompatibles entre elles (options incluses comprises), ou null. */
    public static List<OfferOption> incompatiblePair(final Offer offer, final List<String> optionIds) {
        final var active = selectedOptions(offer, optionIds);
        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                if (active.get(i).isIncompatibleWith(active.get(j))) {
                    return List.of(active.get(i), active.get(j));
                }
            }
        }
        return null;
    }

    /** Prix du terminal au niveau de l'offre choisie (niveau de subvention). */
    public static double terminalPrice(final Terminal terminal, final Offer offer) {
        final var price = terminal.getPrices() == null ? null : terminal.getPrices().get(offer.getLevel());
        if (price == null) {
            throw new IllegalStateException("Pas de prix " + offer.getLevel() + " pour le terminal " + terminal.getId());
        }
        return price;
    }

    /**
     * Territoire déduit du code postal : Saint-Martin (97150) et Saint-Barthélemy (97133) avant le reste de la
     * Guadeloupe (971), puis Martinique (972) et Guyane (973) ; sinon France. Vide sans code postal.
     */
    public static String territoire(final String codePostal) {
        final var cp = codePostal == null ? "" : codePostal.trim();
        if (cp.isEmpty()) {
            return "";
        }
        if (cp.equals("97150")) {
            return "ST-MARTIN";
        }
        if (cp.equals("97133")) {
            return "ST-BARTHELEMY";
        }
        return switch (cp.length() >= 3 ? cp.substring(0, 3) : cp) {
            case "971" -> "GUADELOUPE";
            case "972" -> "MARTINIQUE";
            case "973" -> "GUYANE FRANCAISE";
            default -> "FRANCE";
        };
    }

    /** Territoire de l'adresse : celui transmis, ou à défaut celui déduit du code postal. */
    public static Address withTerritoire(final Address address) {
        if (address != null && isBlank(address.getTerritoire())) {
            address.setTerritoire(territoire(address.getCodePostal()));
        }
        return address;
    }

    /** Carte Prépayée : son prix est un tarif unique, réglé en caisse, et non une mensualité. */
    public static boolean hasOneOffPrice(final Offer offer) {
        return offer != null && offer.getFamily() == FamilyId.prepaye;
    }

    /** Tarif unique de l'offre à régler en caisse (Carte Prépayée), 0 pour une offre à mensualité. */
    public static double oneOffPrice(final Offer offer) {
        return hasOneOffPrice(offer) ? round(offer.getPrice()) : 0;
    }

    /**
     * Mensualité hors remises temporaires : prix de l'offre, options payantes, remise Open ML. Le prix d'une Carte
     * Prépayée n'entre pas dans la mensualité (tarif unique).
     */
    public static double monthly(final Offer offer, final List<String> optionIds, final boolean openMl) {
        final double opts = selectedOptions(offer, optionIds).stream().filter(o -> !o.isIncluded()).mapToDouble(OfferOption::getPrice).sum();
        final double base = hasOneOffPrice(offer) ? 0 : offer.getPrice();
        return Math.max(0, round(base + opts - (openMl ? openMlDiscount(offer) : 0)));
    }

    /** Mensualité pendant la remise de l'offre (appliquée automatiquement) ; null si l'offre n'a pas de remise. */
    public static Double monthlyDuringPromo(final Offer offer, final double monthly) {
        return offer.getPromo() == null ? null : Math.max(0, round(monthly - offer.getPromo().getMonthlyDiscount()));
    }

    public static double simChangeFee(final String reasonId) {
        return ReferenceData.simReasons().stream().anyMatch(r -> r.getId().equals(reasonId) && r.isFee()) ? ReferenceData.SIM_LOSS_FEE : 0;
    }

    /** Montant à régler en caisse pour une acquisition : tarif de la Carte Prépayée et terminal éventuel. */
    public static double acquisitionCashDue(final Offer offer, final double terminalPrice) {
        return round(oneOffPrice(offer) + terminalPrice);
    }

    /** Pas de contrat pour une modification d'options. */
    public static boolean requiresContract(final Flow flow) {
        return flow != Flow.options;
    }

    public static double round(final double n) {
        return Math.round(n * 100) / 100.;
    }

    private static String norm(final String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
