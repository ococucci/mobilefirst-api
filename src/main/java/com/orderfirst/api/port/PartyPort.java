package com.orderfirst.api.port;

import com.orderfirst.api.model.Address;
import com.orderfirst.api.model.Customer;
import com.orderfirst.api.model.Identity;
import com.orderfirst.api.model.Score;

import java.util.Optional;

/** API Party (appy) : identification, recherche d'existence et création du client. */
public interface PartyPort {
    Optional<Customer> findByMsisdn(String msisdn);

    Optional<Customer> findById(String id);

    /** Recherche d'existence sur nom, prénom, date de naissance et pièce. */
    Optional<Customer> findExisting(Identity identity);

    Customer create(Identity identity, Address address, Score score);

    Customer update(String id, Identity identity, Address address, Score score);
}
