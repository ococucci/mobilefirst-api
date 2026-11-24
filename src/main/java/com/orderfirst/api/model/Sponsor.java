package com.orderfirst.api.model;

/**
 * Sponsor Open ML trouvé par api RHOC : N° de mobile ou ND du sponsor, son nom et son prénom, et l'identifiant du
 * jeton émis par RHOC pour cette vérification (à conserver avec la commande).
 */
public record Sponsor(String msisdn, String lastName, String firstName, String tokenId) {
}
