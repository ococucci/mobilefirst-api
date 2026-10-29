package com.orderfirst.api.port;

import java.util.List;

/** Api-ORAS : complétion et fiabilisation de l'adresse. */
public interface OrasPort {
    record AddressSuggestion(String ligne, String codePostal, String commune) {
    }

    List<AddressSuggestion> search(String query);
}
