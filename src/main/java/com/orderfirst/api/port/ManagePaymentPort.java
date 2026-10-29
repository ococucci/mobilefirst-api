package com.orderfirst.api.port;

/** Enabler managePayment : enregistrement d'un paiement effectué en caisse. */
public interface ManagePaymentPort {
    String recordCashPayment(String orderNumber, double amount);
}
