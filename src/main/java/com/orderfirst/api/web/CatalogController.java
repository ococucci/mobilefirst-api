package com.orderfirst.api.web;

import com.orderfirst.api.model.Country;
import com.orderfirst.api.model.Offer;
import com.orderfirst.api.model.SimReason;
import com.orderfirst.api.domain.ReferenceData;
import com.orderfirst.api.port.CountryPort;
import com.orderfirst.api.port.RessourcePort;
import com.orderfirst.api.port.RhocPort;
import com.orderfirst.api.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catalogue", description = "Castor, promo advice, numéros disponibles et Open ML")
@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CatalogService catalog;
    private final RessourcePort ressource;
    private final RhocPort rhoc;
    private final CountryPort countries;

    public CatalogController(final CatalogService catalog, final RessourcePort ressource, final RhocPort rhoc, final CountryPort countries) {
        this.catalog = catalog;
        this.ressource = ressource;
        this.rhoc = rhoc;
        this.countries = countries;
    }

    @Operation(summary = "Forfaits avec, pour chacun, ses options, sa remise (appliquée automatiquement, une seule par offre) et l'éligibilité Open ML "
            + "(Catalogue Castor et api-promo advice)")
    @GetMapping("/catalog/offers")
    public List<Offer> offers(
            @Parameter(description = "Territoire de l'adresse du client (ex. GUADELOUPE) : seules les offres vendables sur ce territoire")
            @RequestParam(required = false) final String territoire) {
        return catalog.offers(territoire);
    }

    @Operation(summary = "Raisons de changement de SIM (frais de 10 € pour perte ou vol)")
    @GetMapping("/catalog/sim-reasons")
    public List<SimReason> simReasons() {
        return ReferenceData.simReasons();
    }

    @Operation(summary = "Pays (id, libellé) proposés pour la nationalité d'origine du client, triés par libellé")
    @GetMapping("/catalog/countries")
    public List<Country> countries() {
        return countries.countries();
    }

    @Operation(summary = "Opérateurs donneurs pour la portabilité")
    @GetMapping("/catalog/donor-operators")
    public List<String> donorOperators() {
        return ReferenceData.DONOR_OPERATORS;
    }

    @Operation(summary = "Numéros de mobile disponibles (api ressource)")
    @GetMapping("/numbers/available")
    public List<String> availableNumbers() {
        return ressource.availableNumbers();
    }

    public record SponsorCheck(String sponsor) {
    }

    /**
     * Résultat de la recherche du sponsor : s'il est reconnu, son N° (format national), son nom, son prénom et
     * l'identifiant du jeton RHOC à renvoyer avec la commande (openMlTokenId).
     */
    public record SponsorResult(boolean valid, String msisdn, String lastName, String firstName, String tokenId) {
    }

    @Operation(summary = "Recherche du sponsor Open ML par N° de mobile ou ND (api RHOC)")
    @PostMapping("/open-ml/sponsor-checks")
    public SponsorResult checkSponsor(@RequestBody final SponsorCheck request) {
        return rhoc.sponsor(request.sponsor())
                .map(s -> new SponsorResult(true, s.msisdn(), s.lastName(), s.firstName(), s.tokenId()))
                .orElseGet(() -> new SponsorResult(false, null, null, null, null));
    }
}
