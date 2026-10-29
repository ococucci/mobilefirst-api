package com.orderfirst.api.web;

import com.orderfirst.api.service.ContractService;
import com.orderfirst.api.service.OrderRequest;
import com.orderfirst.api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Commande", description = "Contrat (api EPC, Api-document), commande (api-order, PubSub) et encaissement (managePayment)")
@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private final OrderService orders;
    private final ContractService contracts;

    public OrderController(final OrderService orders, final ContractService contracts) {
        this.orders = orders;
        this.contracts = contracts;
    }

    public record ContractResponse(String documentId, String downloadUrl) {
    }

    @Operation(summary = "Génère le contrat de la commande (sauf modification d'options) et le stocke")
    @PostMapping("/contracts")
    @ResponseStatus(HttpStatus.CREATED)
    public ContractResponse contract(@RequestBody final OrderRequest request) {
        final var id = contracts.generate(request);
        return new ContractResponse(id, "/api/v1/contracts/" + id);
    }

    @Operation(summary = "Téléchargement du contrat (PDF)")
    @GetMapping(value = "/contracts/{documentId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable final String documentId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contrat-" + documentId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contracts.find(documentId));
    }

    @Operation(summary = "Valide et crée la commande ; elle est publiée dans le bus PubSub")
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderService.CreatedOrder create(@RequestBody final OrderRequest request) {
        return orders.create(request);
    }

    public record CashPayment(Double amount) {
    }

    public record PaymentResponse(String reference) {
    }

    @Operation(summary = "Enregistre le paiement effectué en caisse (enabler managePayment)")
    @PostMapping("/orders/{orderNumber}/cash-payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse pay(@PathVariable final String orderNumber, @RequestBody final CashPayment payment) {
        return new PaymentResponse(orders.recordCashPayment(orderNumber, payment.amount()));
    }
}
