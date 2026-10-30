package com.orderfirst.api.web;

import com.orderfirst.api.domain.Model;
import com.orderfirst.api.domain.Model.Offer;
import com.orderfirst.api.domain.Model.SimReason;
import com.orderfirst.api.port.RessourcePort;
import com.orderfirst.api.port.RhocPort;
import com.orderfirst.api.service.CatalogService;
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
    private final CatalogService catalog;
    private final RessourcePort ressource;
    private final RhocPort rhoc;

    public CatalogController(final CatalogService catalog, final RessourcePort ressource, final RhocPort rhoc) {
        this.catalog = catalog;
        this.ressource = ressource;
        this.rhoc = rhoc;
    }

    @Operation(summary = "Forfaits avec, pour chacun, ses options, sa remise (appliquée automatiquement, une seule par offre) et l'éligibilité Open ML "
            + "(Catalogue Castor et api-promo advice)")
    @GetMapping("/catalog/offers")
    public List<Offer> offers() {
        return catalog.offers();
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
