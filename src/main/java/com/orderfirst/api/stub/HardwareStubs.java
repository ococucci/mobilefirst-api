package com.orderfirst.api.stub;

import com.orderfirst.api.model.IbanToken;
import com.orderfirst.api.model.SepaMandate;
import com.orderfirst.api.model.Terminal;
import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.port.ImlPort;
import com.orderfirst.api.port.SiFinancierPort;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/** Bouchons du matériel et du paiement : enabler si-financier et api IML. */
final class HardwareStubs {
    private HardwareStubs() {
    }

    @Stub
    static class SiFinancier implements SiFinancierPort {
        private final Set<String> stock = ConcurrentHashMap.newKeySet();

        SiFinancier() {
            stock.addAll(StubData.ICCID_STOCK);
        }

        @Override
        public List<Terminal> terminals() {
            return StubData.TERMINALS;
        }

        @Override
        public IccidCheck checkIccid(final String iccid) {
            final var value = Rules.compact(iccid);
            if (!Rules.isValidIccid(value)) {
                return IccidCheck.format;
            }
            return stock.contains(value) ? IccidCheck.ok : IccidCheck.inconnu;
        }

        @Override
        public String scanSim() {
            final var available = stock.stream().sorted().toList();
            return available.isEmpty() ? "" : available.get(ThreadLocalRandom.current().nextInt(available.size()));
        }

        @Override
        public void consumeIccid(final String iccid) {
            stock.remove(Rules.compact(iccid));
        }
    }

    @Stub
    static class Iml implements ImlPort {
        private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private final Map<String, SepaMandate> mandates = new ConcurrentHashMap<>();
        private final AtomicInteger sequence = new AtomicInteger(1);

        @Override
        public SepaMandate storeMandate(final String holder, final String iban) {
            if (Rules.isBlank(holder) || !Rules.isValidIban(iban)) {
                throw new IllegalArgumentException("Mandat SEPA refusé : titulaire ou IBAN invalide");
            }
            final var compact = Rules.compact(iban);
            final int n = sequence.getAndIncrement();
            final var ref = "RUM-OOF-%s-%04d".formatted(compact.substring(compact.length() - 4), n);
            final var mandate = new SepaMandate(ref, tokenize(String.valueOf(n), compact));
            mandates.put(ref, mandate);
            return mandate;
        }

        @Override
        public Optional<SepaMandate> mandate(final String mandateRef) {
            return mandateRef == null ? Optional.empty() : Optional.ofNullable(mandates.get(mandateRef));
        }

        /**
         * Jeton bouchonné, au format d'un RIB français : 4 premiers caractères de l'IBAN, code banque (5), code guichet
         * (5), numéro de compte (11, dont les 7 premiers sont remplacés par des lettres) et clé RIB (2).
         */
        private static IbanToken tokenize(final String uid, final String iban) {
            final var bban = (iban.substring(4) + "0".repeat(23)).substring(0, 23);
            final var account = bban.substring(10, 21);
            final var masked = new StringBuilder();
            final var random = new java.util.Random(iban.hashCode());
            for (int i = 0; i < 7; i++) {
                masked.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
            }
            return new IbanToken(uid, iban.substring(0, 4), bban.substring(0, 5), bban.substring(5, 10),
                    masked + account.substring(7), bban.substring(21, 23));
        }
    }
}
