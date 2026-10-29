package com.orderfirst.api.domain;

import com.orderfirst.api.domain.Model.Address;
import com.orderfirst.api.domain.Model.Identity;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.orderfirst.api.domain.Rules.isBlank;

/**
 * Messages d'erreur champ par champ, identiques à ceux du front (domain/validation.ts).
 * Les clés sont des chemins JSON (ex. {@code holder.nom}) pour que le front place chaque message sous son champ.
 */
public final class Validation {
    public static final String MSISDN_MESSAGE = "Numéro invalide : 10 chiffres commençant par 06 ou 07.";

    private final Map<String, String> errors = new LinkedHashMap<>();

    public Validation check(final boolean invalid, final String field, final String message) {
        if (invalid) {
            errors.putIfAbsent(field, message);
        }
        return this;
    }

    public boolean has(final String field) {
        return errors.containsKey(field);
    }

    public Map<String, String> errors() {
        return errors;
    }

    public boolean isEmpty() {
        return errors.isEmpty();
    }

    /** Lève une erreur 400 listant tous les champs en erreur. */
    public void throwIfInvalid() {
        if (!errors.isEmpty()) {
            throw ApiException.invalidFields(errors);
        }
    }

    public Validation identity(final String prefix, final Identity i, final boolean full, final LocalDate today) {
        if (i == null) {
            return check(true, prefix, "L'identité du titulaire est obligatoire.");
        }
        check(isBlank(i.nom()), prefix + ".nom", "Le nom est obligatoire.");
        check(isBlank(i.prenom()), prefix + ".prenom", "Le prénom est obligatoire.");
        if (isBlank(i.dateNaissance())) {
            check(true, prefix + ".dateNaissance", "La date de naissance est obligatoire.");
        } else {
            final var birth = parseDate(i.dateNaissance());
            check(birth == null || birth.isBefore(LocalDate.of(1900, 1, 1)), prefix + ".dateNaissance", "Date de naissance invalide.");
            check(birth != null && !birth.isBefore(today), prefix + ".dateNaissance", "La date de naissance doit être dans le passé.");
        }
        check(i.pieceType() == null, prefix + ".pieceType", "Le type de pièce est obligatoire.");
        if (isBlank(i.pieceNumero())) {
            check(true, prefix + ".pieceNumero", "Le n° de la pièce est obligatoire.");
        } else {
            check(!Rules.isValidPieceNumber(i.pieceNumero()), prefix + ".pieceNumero", "N° invalide : 9 ou 12 lettres et chiffres.");
        }
        if (full) {
            check(isBlank(i.departementNaissance()), prefix + ".departementNaissance", "Le département de naissance est obligatoire.");
            if (isBlank(i.mobileContact())) {
                check(true, prefix + ".mobileContact", "Le mobile de contact est obligatoire.");
            } else {
                check(!Rules.isValidMsisdn(i.mobileContact()), prefix + ".mobileContact", MSISDN_MESSAGE);
            }
            if (isBlank(i.email())) {
                check(true, prefix + ".email", "L'email est obligatoire.");
            } else {
                check(!Rules.isValidEmail(i.email()), prefix + ".email", "Email invalide (exemple : nom@domaine.fr).");
            }
        }
        return this;
    }

    public Validation address(final String prefix, final Address a) {
        return check(a == null || isBlank(a.ligne()) || isBlank(a.codePostal()) || isBlank(a.commune()),
                prefix, "L'adresse de facturation est obligatoire.");
    }

    public static LocalDate parseDate(final String iso) {
        try {
            return LocalDate.parse(iso);
        } catch (final RuntimeException e) {
            return null;
        }
    }
}
