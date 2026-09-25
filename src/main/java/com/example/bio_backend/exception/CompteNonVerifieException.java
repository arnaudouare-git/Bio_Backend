package com.example.bio_backend.exception;

/**
 * Levee quand un Producteur tente une action sensible (publier un produit,
 * plus tard : passer/confirmer une commande, payer...) alors que son compte
 * n'est pas encore VERIFIE (formation + verification CNIB non finalisees par
 * un Administrateur -- voir AdminService.activerCompte).
 *
 * C'est le controle d'acces qui etait note comme dette technique depuis le
 * module Authentification (voir AuthService) : il est cable ici pour la
 * premiere fois, sur l'action "publier un produit".
 */
public class CompteNonVerifieException extends RuntimeException {
    public CompteNonVerifieException(String message) {
        super(message);
    }
}
