package com.orderfirst.api.port;

import com.orderfirst.api.domain.Model.Terminal;

import java.util.List;

/** Enabler si-financier : matériel (SIM, terminal) et contrôle de l'ICCID. */
public interface SiFinancierPort {
    enum IccidCheck { ok, format, inconnu }

    List<Terminal> terminals();

    IccidCheck checkIccid(String iccid);

    /** Simule la lecture du code-barres d'une SIM du stock boutique. */
    String scanSim();

    /** Sort la SIM du stock une fois la commande validée. */
    void consumeIccid(String iccid);
}
