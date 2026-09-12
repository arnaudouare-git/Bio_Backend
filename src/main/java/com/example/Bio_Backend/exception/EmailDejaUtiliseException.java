package com.example.bio_backend.exception;

/** Levee quand on tente de s'inscrire avec un email deja present en base. */
public class EmailDejaUtiliseException extends RuntimeException {
    public EmailDejaUtiliseException(String email) {
        super("Un compte existe deja avec l'email : " + email);
    }
}
