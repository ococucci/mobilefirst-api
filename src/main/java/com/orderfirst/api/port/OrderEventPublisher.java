package com.orderfirst.api.port;

/** Publication des commandes validées dans le bus PubSub (comme MOC / MobileOnLine). */
public interface OrderEventPublisher {
    String TOPIC = "orange-order-first.mobile.order-validated";

    void publish(Object event);
}
