package com.orderfirst.api.stub;

import com.orderfirst.api.domain.Model.Offer;
import com.orderfirst.api.domain.Model.Promo;
import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.port.CataloguePort;
import com.orderfirst.api.port.PromoAdvicePort;
import com.orderfirst.api.port.RessourcePort;
import com.orderfirst.api.port.RhocPort;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Bouchons du catalogue : Castor, promo advice, RHOC (Open ML) et ressource (numéros). */
final class CatalogueStubs {
    private CatalogueStubs() {
    }

    @Stub
    static class Castor implements CataloguePort {
        @Override
        public List<Offer> offers() {
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
        @Override
        public boolean isValidSponsor(final String sponsor) {
            return StubData.SPONSORS.contains(Rules.digits(sponsor));
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
