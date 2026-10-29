package com.orderfirst.api.web;

import com.orderfirst.api.domain.Model;
import com.orderfirst.api.domain.Model.MobileOption;
import com.orderfirst.api.domain.Model.Offer;
import com.orderfirst.api.domain.Model.Promo;
import com.orderfirst.api.domain.Model.SimReason;
import com.orderfirst.api.port.CataloguePort;
import com.orderfirst.api.port.PromoAdvicePort;
import com.orderfirst.api.port.RessourcePort;
import com.orderfirst.api.port.RhocPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catalogue", description = "Castor, promo advice, numéros disponibles et Open ML")
@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CataloguePort catalogue;
    private final PromoAdvicePort promos;
    private final RessourcePort ressource;
    private final RhocPort rhoc;

    public CatalogController(final CataloguePort catalogue, final PromoAdvicePort promos, final RessourcePort ressource, final RhocPort rhoc) {
        this.catalogue = catalogue;
        this.promos = promos;
        this.ressource = ressource;
        this.rhoc = rhoc;
    }

    @Operation(summary = "Forfaits (Catalogue Castor)")
    @GetMapping("/catalog/offers")
    public List<Offer> offers() {
        return catalogue.offers();
    }

    @Operation(summary = "Options mobile (Catalogue Castor)")
    @GetMapping("/catalog/options")
    public List<MobileOption> options() {
        return catalogue.options();
    }

    @Operation(summary = "Remises applicables (api-promo advice)")
    @GetMapping("/catalog/promotions")
    public List<Promo> promotions() {
        return promos.promos();
    }

    @Operation(summary = "Raisons de changement de SIM (frais de 10 € pour perte ou vol)")
    @GetMapping("/catalog/sim-reasons")
    public List<SimReason> simReasons() {
        return Model.SIM_REASONS;
    }

    @Operation(summary = "Opérateurs donneurs pour la portabilité")
    @GetMapping("/catalog/donor-operators")
    public List<String> donorOperators() {
        return Model.DONOR_OPERATORS;
    }

    @Operation(summary = "Numéros de mobile disponibles (api ressource)")
    @GetMapping("/numbers/available")
    public List<String> availableNumbers() {
        return ressource.availableNumbers();
    }

    public record SponsorCheck(String sponsor) {
    }

    public record SponsorResult(boolean valid) {
    }

    @Operation(summary = "Validation du N° sponsor Open ML (api RHOC)")
    @PostMapping("/open-ml/sponsor-checks")
    public SponsorResult checkSponsor(@RequestBody final SponsorCheck request) {
        return new SponsorResult(rhoc.isValidSponsor(request.sponsor()));
    }
}
