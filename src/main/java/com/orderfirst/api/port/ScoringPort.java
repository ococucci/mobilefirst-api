package com.orderfirst.api.port;

import com.orderfirst.api.domain.Model.Customer;
import com.orderfirst.api.domain.Model.Identity;
import com.orderfirst.api.domain.Model.Score;

/** api-scoring : contrôle Préventel. */
public interface ScoringPort {
    Score preventel(Identity identity);

    /** Re-scoring d'un client en base (ligne Card qui change de forfait). */
    Score rescore(Customer customer);
}
