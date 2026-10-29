package com.orderfirst.api.stub;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Implémentation bouchonnée d'une API du SI, active tant que
 * {@code orderfirst.integrations=stub} (valeur par défaut).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@ConditionalOnProperty(name = "orderfirst.integrations", havingValue = "stub", matchIfMissing = true)
public @interface Stub {
}
