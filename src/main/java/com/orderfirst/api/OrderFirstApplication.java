package com.orderfirst.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OrderFirstApplication {
    public static void main(final String[] args) {
        SpringApplication.run(OrderFirstApplication.class, args);
    }
}
