package com.example.bio_backend.exception;

/**
 * Levee quand un Administrateur tente d'activer le compte d'un Producteur
 * qui n'a pas encore suivi de formation (regle metier : formation obligatoire
 * avant activation, voir AdminService.activerCompte).
 */
public class CompteNonEligibleActivationException extends RuntimeException {
    public CompteNonEligibleActivationException(String message) {
        super(message);
    }
}
