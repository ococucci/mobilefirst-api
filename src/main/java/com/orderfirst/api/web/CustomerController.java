package com.orderfirst.api.web;

import com.orderfirst.api.domain.Model.Address;
import com.orderfirst.api.domain.Model.Customer;
import com.orderfirst.api.domain.Model.Identity;
import com.orderfirst.api.domain.Model.Score;
import com.orderfirst.api.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Client", description = "Identification (Party, Préventel) et informations du client")
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customers;

    public CustomerController(final CustomerService customers) {
        this.customers = customers;
    }

    @Operation(summary = "Recherche du client par MSISDN (404 si le numéro est inconnu)")
    @GetMapping
    public Customer byMsisdn(@RequestParam final String msisdn) {
        return customers.byMsisdn(msisdn);
    }

    @Operation(summary = "Client par identifiant")
    @GetMapping("/{id}")
    public Customer byId(@PathVariable final String id) {
        return customers.byId(id);
    }

    public record IdentityRequest(Identity identity) {
    }

    public record ExistenceResponse(Customer existing) {
    }

    @Operation(summary = "Recherche d'existence (nom, prénom, date de naissance, pièce) avant création d'un client sans numéro")
    @PostMapping("/existence-checks")
    public ExistenceResponse existence(@RequestBody final IdentityRequest request) {
        return new ExistenceResponse(customers.findExisting(request.identity()).orElse(null));
    }

    public record CreateRequest(Identity identity, Address address) {
    }

    @Operation(summary = "Contrôle Préventel puis création du client (pas de création si le score est rouge)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerService.Creation create(@RequestBody final CreateRequest request) {
        return customers.create(request.identity(), request.address());
    }

    @Operation(summary = "Mise à jour à l'étape Client (relance Préventel si nom, prénom ou date de naissance changent)")
    @PutMapping("/{id}")
    public CustomerService.Update update(@PathVariable final String id, @RequestBody final CreateRequest request) {
        return customers.update(id, request.identity(), request.address());
    }

    @Operation(summary = "Lignes du client ayant une commande en cours (api-order)")
    @GetMapping("/{id}/pending-lines")
    public List<String> pendingLines(@PathVariable final String id) {
        return customers.pendingLines(id);
    }

    public record ScoreResponse(Score score) {
    }

    @Operation(summary = "Re-scoring Préventel d'une ligne Card avant changement de forfait")
    @PostMapping("/{id}/lines/{msisdn}/rescoring")
    public ScoreResponse rescore(@PathVariable final String id, @PathVariable final String msisdn) {
        return new ScoreResponse(customers.rescoreCardLine(id, msisdn));
    }
}
