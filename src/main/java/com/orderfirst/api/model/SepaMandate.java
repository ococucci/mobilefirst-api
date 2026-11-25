package com.orderfirst.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/** Mandat SEPA enregistré par api IML : référence unique du mandat (RUM) et information bancaire tokenisée. */
@Schema(description = "Mandat SEPA enregistré (api IML)")
public record SepaMandate(
        @Schema(example = "RUM-OOF-0189-0001", requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Référence unique du mandat (RUM), à renvoyer dans la commande (sepaMandateRef)")
        String reference,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Information bancaire tokenisée, à renvoyer dans la commande (ibanToken)")
        IbanToken ibanToken) {
}
