package com.orderfirst.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {
    /** Heure des Antilles : les dates (portage, naissance) sont contrôlées dans le fuseau des boutiques. */
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Guadeloupe"));
    }
}
