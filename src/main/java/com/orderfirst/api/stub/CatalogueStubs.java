package com.orderfirst.api.stub;

import com.orderfirst.api.model.Offer;
import com.orderfirst.api.model.Promo;
import com.orderfirst.api.model.Sponsor;
import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.port.CataloguePort;
import com.orderfirst.api.port.PromoAdvicePort;
import com.orderfirst.api.port.RessourcePort;
import com.orderfirst.api.port.RhocPort;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Bouchons du catalogue : Castor, promo advice, RHOC (Open ML) et ressource (numéros). */
final class CatalogueStubs {
    private CatalogueStubs() {
    }

    @Stub
    static class Castor implements CataloguePort {
        /** Le bouchon renvoie les mêmes offres quel que soit le territoire. */
        @Override
        public List<Offer> offers(final String territoire) {
            return StubData.OFFERS;
        }
    }

    @Stub
    static class PromoAdvice implements PromoAdvicePort {
        @Override
        public Optional<Promo> promoFor(final String offerId) {
            return Optional.ofNullable(StubData.PROMOS.get(offerId));
        }
    }

    @Stub
    static class Rhoc implements RhocPort {
        /** Un nouveau jeton à chaque vérification, comme api RHOC. */
        @Override
        public Optional<Sponsor> sponsor(final String msisdn) {
            final var national = Rules.nationalNumber(msisdn);
            final var names = StubData.SPONSORS.get(national);
            return names == null ? Optional.empty()
                    : Optional.of(new Sponsor(national, names.get(0), names.get(1), "RHOC-" + UUID.randomUUID()));
        }
    }

    @Stub
    static class Ressource implements RessourcePort {
        private final Set<String> reserved = ConcurrentHashMap.newKeySet();

        @Override
        public List<String> availableNumbers() {
            return StubData.AVAILABLE_NUMBERS.stream().filter(n -> !reserved.contains(n)).toList();
        }

        @Override
        public boolean reserve(final String msisdn) {
            final var n = Rules.digits(msisdn);
            return StubData.AVAILABLE_NUMBERS.contains(n) && reserved.add(n);
        }
    }
}
