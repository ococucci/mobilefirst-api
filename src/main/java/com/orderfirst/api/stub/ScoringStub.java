package com.orderfirst.api.stub;

import com.orderfirst.api.domain.Model.Customer;
import com.orderfirst.api.domain.Model.Identity;
import com.orderfirst.api.domain.Model.Score;
import com.orderfirst.api.port.ScoringPort;

import java.util.Locale;

/** Bouchon : un nom contenant « rouge » ou « orange » renvoie ce score, sinon vert. */
@Stub
class ScoringStub implements ScoringPort {
    @Override
    public Score preventel(final Identity identity) {
        final var nom = identity.nom() == null ? "" : identity.nom().toLowerCase(Locale.ROOT);
        return nom.contains("rouge") ? Score.rouge : nom.contains("orange") ? Score.orange : Score.vert;
    }

    @Override
    public Score rescore(final Customer customer) {
        final var known = StubData.RESCORE.get(customer.id());
        return known != null ? known : preventel(customer.identity());
    }
}
