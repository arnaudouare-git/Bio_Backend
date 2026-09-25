package com.example.bio_backend.exception;

/** Levee quand une Formation ou un Producteur cible par id n'existe pas. */
public class RessourceIntrouvableException extends RuntimeException {
    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
