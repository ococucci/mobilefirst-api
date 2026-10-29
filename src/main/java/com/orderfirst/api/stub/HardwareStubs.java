package com.orderfirst.api.stub;

import com.orderfirst.api.domain.Model.Terminal;
import com.orderfirst.api.domain.Rules;
import com.orderfirst.api.port.ImlPort;
import com.orderfirst.api.port.SiFinancierPort;

import java.util.List;
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
        private final Set<String> mandates = ConcurrentHashMap.newKeySet();
        private final AtomicInteger sequence = new AtomicInteger(1);

        @Override
        public String storeMandate(final String holder, final String iban) {
            if (Rules.isBlank(holder) || !Rules.isValidIban(iban)) {
                throw new IllegalArgumentException("Mandat SEPA refusé : titulaire ou IBAN invalide");
            }
            final var compact = Rules.compact(iban);
            final var ref = "RUM-OOF-%s-%04d".formatted(compact.substring(compact.length() - 4), sequence.getAndIncrement());
            mandates.add(ref);
            return ref;
        }

        @Override
        public boolean exists(final String mandateRef) {
            return mandateRef != null && mandates.contains(mandateRef);
        }
    }
}
