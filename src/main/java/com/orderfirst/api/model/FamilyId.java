package com.orderfirst.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Famille de forfait, exposée en JSON sous son code (ex. « non-bloque »). */
public enum FamilyId {
    bloque("bloque"), nonBloque("non-bloque"), internet("internet"), prepaye("prepaye");

    private final String code;

    FamilyId(final String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static FamilyId of(final String code) {
        for (final var f : values()) {
            if (f.code.equals(code)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Famille inconnue : " + code);
    }
}
