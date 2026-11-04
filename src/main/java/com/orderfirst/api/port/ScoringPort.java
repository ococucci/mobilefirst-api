package com.orderfirst.api.port;

import com.orderfirst.api.model.Customer;
import com.orderfirst.api.model.Identity;
import com.orderfirst.api.model.Score;

/** api-scoring : contrôle Préventel. */
public interface ScoringPort {
    Score preventel(Identity identity);

    /** Re-scoring d'un client en base (ligne Card qui change de forfait). */
    Score rescore(Customer customer);
}
