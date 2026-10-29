package com.orderfirst.api.service;

import com.orderfirst.api.domain.Model.Address;
import com.orderfirst.api.domain.Model.Flow;
import com.orderfirst.api.domain.Model.Identity;
import com.orderfirst.api.domain.Model.SimType;

import java.util.List;

/**
 * Commande saisie dans le tunnel. Les champs utiles dépendent de la demande ({@code flow}) :
 * <ul>
 *   <li>acquisition : holder, billingAddress, number, offerId, options, openMlSponsor, sim, terminal, sepaMandateRef ;</li>
 *   <li>forfait : lineMsisdn, offerId, options, openMlSponsor ;</li>
 *   <li>options : lineMsisdn, options (liste complète souhaitée, le serveur calcule ajouts et retraits) ;</li>
 *   <li>sim : lineMsisdn, simReason, sim.</li>
 * </ul>
 * Contrat (sauf options) : contractRef issu de POST /contracts et contractSigned.
 */
public record OrderRequest(
        Flow flow,
        String customerId,
        String lineMsisdn,
        Identity holder,
        Address billingAddress,
        NumberChoice number,
        String offerId,
        List<String> options,
        String openMlSponsor,
        SimChoice sim,
        TerminalChoice terminal,
        String sepaMandateRef,
        String simReason,
        String contractRef,
        Boolean contractSigned,
        Boolean cgvAccepted,
        Boolean privacyAccepted) {

    public List<String> optionsOrEmpty() {
        return options == null ? List.of() : options;
    }

    /** Nouveau numéro choisi dans la liste, ou portabilité (les quatre champs sont obligatoires). */
    public record NumberChoice(Mode mode, String msisdn, String rio, String portedMsisdn, String operator, String portingDate) {
        public enum Mode { nouveau, portabilite }
    }

    public record SimChoice(SimType type, String iccid) {
    }

    public record TerminalChoice(String id, String imei) {
    }
}
