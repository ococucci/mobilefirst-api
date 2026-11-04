package com.orderfirst.api.stub;

import com.orderfirst.api.model.Customer;
import com.orderfirst.api.model.Identity;
import com.orderfirst.api.model.Score;
import com.orderfirst.api.port.ScoringPort;

import java.util.Locale;

/** Bouchon : un nom contenant « rouge » ou « orange » renvoie ce score, sinon vert. */
@Stub
class ScoringStub implements ScoringPort {
    @Override
    public Score preventel(final Identity identity) {
        final var nom = identity.getNom() == null ? "" : identity.getNom().toLowerCase(Locale.ROOT);
        return nom.contains("rouge") ? Score.rouge : nom.contains("orange") ? Score.orange : Score.vert;
    }

    @Override
    public Score rescore(final Customer customer) {
        final var known = StubData.RESCORE.get(customer.getId());
        return known != null ? known : preventel(customer.getIdentity());
    }
}
