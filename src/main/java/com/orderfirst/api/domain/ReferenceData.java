package com.orderfirst.api.domain;

import com.orderfirst.api.model.SimReason;
import com.orderfirst.api.model.SimReason;

import java.util.List;

/** Listes de référence et montants fixés par la cinématique. */
public final class ReferenceData {
    private ReferenceData() {
    }

    /** Raisons de changement de SIM ; seuls la perte et le vol donnent lieu à des frais. */
    public static List<SimReason> simReasons() {
        return List.of(
                new SimReason("mobile", "Changement de mobile", false),
                new SimReason("technique", "Problème technique", false),
                new SimReason("obsolete", "SIM obsolète", false),
                new SimReason("perte-vol", "SIM perdue ou volée", true),
                new SimReason("endommagee", "SIM endommagée", false),
                new SimReason("esim", "Passage à l'eSIM", false),
                new SimReason("autre", "Autre", false));
    }

    public static final List<String> DONOR_OPERATORS = List.of("SFR Caraibes", "FREE", "Dauphin Telecom", "UTS", "DIGICEL");

    /** Puce Carte Prépayée, à régler en caisse. */
    public static final double CARD_SIM_PRICE = 10;

    /** Frais de changement de SIM pour perte ou vol, à régler en caisse. */
    public static final double SIM_LOSS_FEE = 10;
}
