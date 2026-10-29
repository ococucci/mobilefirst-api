package com.orderfirst.api.port;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** api-order : création de la commande, numéro de commande et commandes en cours. */
public interface OrderPort {
    List<String> linesWithPendingOrder(Collection<String> msisdns);

    CreatedOrder create(String msisdn, Map<String, Object> order);

    record CreatedOrder(String orderNumber, Instant createdAt) {
    }
}
