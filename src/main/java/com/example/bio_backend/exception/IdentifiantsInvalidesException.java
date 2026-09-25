package com.example.bio_backend.exception;

/** Levee quand l'email ou le mot de passe fourni a la connexion est incorrect. */
public class IdentifiantsInvalidesException extends RuntimeException {
    public IdentifiantsInvalidesException() {
        super("Email ou mot de passe incorrect");
    }
}
