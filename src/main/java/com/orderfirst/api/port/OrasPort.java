package com.orderfirst.api.port;

import com.orderfirst.api.model.AddressSuggestion;
import java.util.List;

/** Api-ORAS : complétion et fiabilisation de l'adresse. */
public interface OrasPort {

    List<AddressSuggestion> search(String query);
}
