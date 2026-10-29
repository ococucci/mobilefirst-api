package com.orderfirst.api.stub;

import com.orderfirst.api.port.ManagePaymentPort;
import com.orderfirst.api.port.OrderEventPublisher;
import com.orderfirst.api.port.OrderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Bouchons de la commande : api-order, bus PubSub et managePayment. */
final class OrderStubs {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStubs.class);

    private OrderStubs() {
    }

    @Stub
    static class ApiOrder implements OrderPort {
        private final Set<String> pending = ConcurrentHashMap.newKeySet();
        private final AtomicInteger sequence = new AtomicInteger(98417);

        ApiOrder() {
            pending.addAll(StubData.PENDING_ORDER_LINES);
        }

        @Override
        public List<String> linesWithPendingOrder(final Collection<String> msisdns) {
            return msisdns.stream().filter(pending::contains).toList();
        }

        @Override
        public CreatedOrder create(final String msisdn, final Map<String, Object> order) {
            pending.add(msisdn);
            return new CreatedOrder("CMD-%d-%07d".formatted(Year.now().getValue(), sequence.getAndIncrement()), Instant.now());
        }
    }

    /** Conserve les messages publiés en mémoire et les trace. */
    @Stub
    static class InMemoryBus implements OrderEventPublisher {
        private final List<Object> published = new CopyOnWriteArrayList<>();

        @Override
        public void publish(final Object event) {
            published.add(event);
            LOGGER.info("[PubSub] {} : {}", TOPIC, event);
        }

        List<Object> published() {
            return published;
        }
    }

    @Stub
    static class ManagePayment implements ManagePaymentPort {
        private final AtomicInteger sequence = new AtomicInteger(1);

        @Override
        public String recordCashPayment(final String orderNumber, final double amount) {
            final var ref = "PAY-%s-%d".formatted(orderNumber.substring(4), sequence.getAndIncrement());
            LOGGER.info("[managePayment] {} : {} € ({})", orderNumber, amount, ref);
            return ref;
        }
    }
}
