package com.orderfirst.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * Information bancaire tokenisée renvoyée par api IML à l'enregistrement du mandat SEPA : l'IBAN en clair n'est pas
 * conservé, seuls le jeton et ses éléments (RIB du jeton) circulent.
 */
@Schema(description = "Information bancaire tokenisée (api IML)")
public class IbanToken {

    /** Identifiant unique de l'information bancaire tokenisée. */
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant unique de l'information bancaire tokenisée")
    private String uid;

    /** Les 4 premiers caractères du jeton. */
    @Schema(example = "FR79", requiredMode = Schema.RequiredMode.REQUIRED, description = "Les 4 premiers caractères du jeton")
    private String codeIban;

    /** Code banque du jeton. */
    @Schema(example = "30006", requiredMode = Schema.RequiredMode.REQUIRED, description = "Code banque du jeton")
    private String bankIdentifier;

    /** Code guichet du jeton. */
    @Schema(example = "00148", requiredMode = Schema.RequiredMode.REQUIRED, description = "Code guichet du jeton")
    private String bankBranchIdentifier;

    /** Numéro de compte du jeton. */
    @Schema(example = "YZPWWLX8901", requiredMode = Schema.RequiredMode.REQUIRED, description = "Numéro de compte du jeton")
    private String accountNumber;

    /** Clé RIB du jeton. */
    @Schema(example = "01", requiredMode = Schema.RequiredMode.REQUIRED, description = "Clé RIB du jeton")
    private String ribCheckDigits;

    public IbanToken() {
    }

    public IbanToken(final String uid, final String codeIban, final String bankIdentifier, final String bankBranchIdentifier,
                     final String accountNumber, final String ribCheckDigits) {
        this.uid = uid;
        this.codeIban = codeIban;
        this.bankIdentifier = bankIdentifier;
        this.bankBranchIdentifier = bankBranchIdentifier;
        this.accountNumber = accountNumber;
        this.ribCheckDigits = ribCheckDigits;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    public String getCodeIban() {
        return codeIban;
    }

    public void setCodeIban(final String codeIban) {
        this.codeIban = codeIban;
    }

    public String getBankIdentifier() {
        return bankIdentifier;
    }

    public void setBankIdentifier(final String bankIdentifier) {
        this.bankIdentifier = bankIdentifier;
    }

    public String getBankBranchIdentifier() {
        return bankBranchIdentifier;
    }

    public void setBankBranchIdentifier(final String bankBranchIdentifier) {
        this.bankBranchIdentifier = bankBranchIdentifier;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRibCheckDigits() {
        return ribCheckDigits;
    }

    public void setRibCheckDigits(final String ribCheckDigits) {
        this.ribCheckDigits = ribCheckDigits;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof final IbanToken that
                && Objects.equals(uid, that.uid) && Objects.equals(codeIban, that.codeIban)
                && Objects.equals(bankIdentifier, that.bankIdentifier) && Objects.equals(bankBranchIdentifier, that.bankBranchIdentifier)
                && Objects.equals(accountNumber, that.accountNumber) && Objects.equals(ribCheckDigits, that.ribCheckDigits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, codeIban, bankIdentifier, bankBranchIdentifier, accountNumber, ribCheckDigits);
    }

    @Override
    public String toString() {
        return "IbanToken[uid=" + uid + ", codeIban=" + codeIban + ", bankIdentifier=" + bankIdentifier
                + ", bankBranchIdentifier=" + bankBranchIdentifier + ", accountNumber=" + accountNumber
                + ", ribCheckDigits=" + ribCheckDigits + "]";
    }
}
