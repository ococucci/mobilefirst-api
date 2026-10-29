package com.orderfirst.api.port;

/** api RHOC : validation du N° sponsor Open ML. */
public interface RhocPort {
    boolean isValidSponsor(String sponsor);
}
