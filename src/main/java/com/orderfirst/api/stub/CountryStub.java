package com.orderfirst.api.stub;

import com.orderfirst.api.model.Country;
import com.orderfirst.api.port.CountryPort;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Bouchon du référentiel des pays : identifiants ISO 3166-1 alpha-2, libellés en français. */
@Stub
class CountryStub implements CountryPort {
    private static final List<Country> COUNTRIES = StubData.COUNTRIES.stream()
            .sorted(Comparator.comparing(Country::label, Collator.getInstance(Locale.FRENCH)))
            .toList();

    @Override
    public List<Country> countries() {
        return COUNTRIES;
    }
}
