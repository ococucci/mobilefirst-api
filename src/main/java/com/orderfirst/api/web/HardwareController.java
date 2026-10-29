package com.orderfirst.api.web;

import com.orderfirst.api.domain.Model.Terminal;
import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.domain.Validation;
import com.orderfirst.api.port.ImlPort;
import com.orderfirst.api.port.OrasPort;
import com.orderfirst.api.port.OrasPort.AddressSuggestion;
import com.orderfirst.api.port.SiFinancierPort;
import com.orderfirst.api.port.SiFinancierPort.IccidCheck;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Matériel, paiement et adresse", description = "si-financier, api IML et Api-ORAS")
@RestController
@RequestMapping("/api/v1")
public class HardwareController {
    private final SiFinancierPort siFinancier;
    private final ImlPort iml;
    private final OrasPort oras;

    public HardwareController(final SiFinancierPort siFinancier, final ImlPort iml, final OrasPort oras) {
        this.siFinancier = siFinancier;
        this.iml = iml;
        this.oras = oras;
    }

    @Operation(summary = "Terminaux vendables (enabler si-financier)")
    @GetMapping("/hardware/terminals")
    public List<Terminal> terminals() {
        return siFinancier.terminals();
    }

    public record IccidRequest(String iccid) {
    }

    public record IccidResult(IccidCheck result) {
    }

    @Operation(summary = "Contrôle de l'ICCID : ok, format ou inconnu du stock (enabler si-financier)")
    @PostMapping("/hardware/iccid-checks")
    public IccidResult checkIccid(@RequestBody final IccidRequest request) {
        return new IccidResult(siFinancier.checkIccid(request.iccid()));
    }

    @Operation(summary = "Lecture simulée du code-barres d'une SIM du stock boutique")
    @PostMapping("/hardware/sim-scans")
    public IccidRequest scan() {
        return new IccidRequest(siFinancier.scanSim());
    }

    public record MandateRequest(String holder, String iban) {
    }

    public record MandateResponse(String reference) {
    }

    @Operation(summary = "Contrôle et stockage du mandat SEPA (api IML), signé hors de l'outil")
    @PostMapping("/sepa-mandates")
    @ResponseStatus(HttpStatus.CREATED)
    public MandateResponse mandate(@RequestBody final MandateRequest request) {
        final var v = new Validation().check(Rules.isBlank(request.holder()), "holder", "Le titulaire du compte est obligatoire.");
        if (Rules.isBlank(request.iban())) {
            v.check(true, "iban", "L'IBAN est obligatoire.");
        } else {
            v.check(!Rules.isValidIban(request.iban()), "iban", "IBAN invalide : vérifiez sa saisie.");
        }
        v.throwIfInvalid();
        return new MandateResponse(iml.storeMandate(request.holder(), request.iban()));
    }

    @Operation(summary = "Complétion d'adresse (Api-ORAS)")
    @GetMapping("/addresses")
    public List<AddressSuggestion> addresses(@RequestParam("q") final String query) {
        return oras.search(query);
    }
}
