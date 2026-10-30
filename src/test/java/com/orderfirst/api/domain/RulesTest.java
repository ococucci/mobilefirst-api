package com.orderfirst.api.domain;

import com.orderfirst.api.domain.Model.FamilyId;
import com.orderfirst.api.domain.Model.Identity;
import com.orderfirst.api.domain.Model.Line;
import com.orderfirst.api.domain.Model.OfferOption;
import com.orderfirst.api.domain.Model.OpenMl;
import com.orderfirst.api.domain.Model.Promo;
import com.orderfirst.api.domain.Model.Offer;
import com.orderfirst.api.domain.Model.PieceType;
import com.orderfirst.api.domain.Model.Score;
import com.orderfirst.api.domain.Model.SimType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RulesTest {
    private static final Identity MARIE = new Identity("Mme", "Dupont", "Marie", "1988-03-14", "971", "",
            PieceType.CNI, "12AB34567", "0690123456", "a@b.fr");

    private static Offer offer(final FamilyId family, final double price, final boolean entryLevel) {
        return new Offer("o", family, "Offre", "", price,
                family == FamilyId.prepaye ? OpenMl.NONE : OpenMl.of(entryLevel ? 5 : 10),
                List.of(new OfferOption("a", "A", 5, false), new OfferOption("inc", "Incluse", 0, true)), List.of());
    }

    private static Line line(final boolean card) {
        return new Line("0690123456", "o", "Offre", card ? null : 10., card, SimType.physique, List.of());
    }

    @Test
    void preventelScoreLimitsFamilies() {
        assertEquals(4, Rules.allowedFamilies(Score.vert).size());
        assertEquals(List.of(FamilyId.prepaye), Rules.allowedFamilies(Score.orange));
        assertTrue(Rules.allowedFamilies(Score.rouge).isEmpty());
    }

    @Test
    void onlyNameFirstNameAndBirthDateTriggerRescore() {
        assertTrue(Rules.identityChangeRequiresRescore(MARIE, new Identity("Mme", "Durand", "Marie", "1988-03-14", "971", "", PieceType.CNI, "12AB34567", "", "")));
        assertTrue(Rules.identityChangeRequiresRescore(MARIE, new Identity("Mme", "Dupont", "Marie", "1990-01-01", "971", "", PieceType.CNI, "12AB34567", "", "")));
        assertFalse(Rules.identityChangeRequiresRescore(MARIE, new Identity("Mme", " dupont ", "Marie", "1988-03-14", "972", "", PieceType.PASSEPORT, "ZZ", "", "x@y.fr")));
    }

    @Test
    void openMl() {
        assertEquals(5, Rules.openMlDiscount(offer(FamilyId.bloque, 20, true)));
        assertEquals(10, Rules.openMlDiscount(offer(FamilyId.bloque, 20, false)));
        assertFalse(Rules.isOpenMlAvailable(offer(FamilyId.prepaye, 0, true), null));
        assertFalse(Rules.isOpenMlAvailable(offer(FamilyId.bloque, 20, false), line(true)));
        assertTrue(Rules.isOpenMlAvailable(offer(FamilyId.bloque, 20, false), line(false)));
    }

    @Test
    void monthlyPrice() {
        final var offer = offer(FamilyId.bloque, 22.99, false);
        assertEquals(17.99, Rules.monthly(offer, List.of("a"), true));
        assertEquals(0, Rules.monthly(offer(FamilyId.bloque, 5, true), List.of(), true));
        // L'option incluse est toujours active et gratuite ; une option non proposée est ignorée.
        assertEquals(List.of("a", "inc"), Rules.selectedOptions(offer, List.of("a", "x")).stream().map(OfferOption::id).toList());
        assertEquals(22.99, Rules.monthly(offer, List.of("inc"), false));
    }

    @Test
    void promosApplyAutomatically() {
        final var offer = offer(FamilyId.bloque, 22.99, false);
        assertNull(Rules.monthlyDuringPromo(offer, 17.99));
        final var promoted = offer.withPromos(List.of(new Promo("p", "−10 € / 6 mois", 10, 6)));
        assertEquals(7.99, Rules.monthlyDuringPromo(promoted, 17.99));
        assertEquals(0, Rules.monthlyDuringPromo(promoted, 4));
    }

    @Test
    void cashAmounts() {
        assertEquals(10, Rules.simChangeFee("perte-vol"));
        assertEquals(0, Rules.simChangeFee("mobile"));
        assertEquals(10, Rules.acquisitionCashDue(offer(FamilyId.prepaye, 0, true), 0));
        assertEquals(149, Rules.acquisitionCashDue(offer(FamilyId.bloque, 20, false), 149));
        assertFalse(Rules.requiresContract(Model.Flow.options));
        assertTrue(Rules.requiresContract(Model.Flow.sim));
    }

    @Test
    void inputChecks() {
        assertTrue(Rules.isValidMsisdn("06 90 12 34 56"));
        assertFalse(Rules.isValidMsisdn("0590123456"));
        assertFalse(Rules.isValidMsisdn("06901234AB"));
        assertTrue(Rules.isValidRio("01P123456ABC"));
        assertFalse(Rules.isValidRio("01X123456ABC"));
        assertTrue(Rules.isValidIban("FR76 3000 6000 0112 3456 7890 189"));
        assertFalse(Rules.isValidIban("FR76 3000 6000 0112 3456 7890 188"));
        assertTrue(Rules.isValidIccid("8933 0100 0000 0000 001"));
        assertFalse(Rules.isValidIccid("1234"));
        assertTrue(Rules.isValidImei("490154203237518"));
        assertFalse(Rules.isValidImei("490154203237519"));
        assertTrue(Rules.isValidPieceNumber("12AB34567"));
        assertTrue(Rules.isValidPieceNumber("880692310285"));
        assertFalse(Rules.isValidPieceNumber("X1"));
    }

    @Test
    void identityValidation() {
        final var today = LocalDate.of(2026, 10, 29);
        assertTrue(new Validation().identity("holder", MARIE, true, today).isEmpty());
        final var errors = new Validation().identity("holder",
                new Identity("Mme", " ", "", "2030-01-01", "", "", PieceType.CNI, "X1", "12", "x"), true, today).errors();
        assertEquals(List.of("holder.nom", "holder.prenom", "holder.dateNaissance", "holder.pieceNumero",
                "holder.departementNaissance", "holder.mobileContact", "holder.email"), List.copyOf(errors.keySet()));
        assertEquals("La date de naissance doit être dans le passé.", errors.get("holder.dateNaissance"));
    }
}
