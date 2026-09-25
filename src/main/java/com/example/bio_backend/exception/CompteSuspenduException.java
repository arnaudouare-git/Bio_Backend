package com.example.bio_backend.exception;

/**
 * Levee quand un utilisateur dont le compte a ete suspendu par un
 * Administrateur (voir AdminService.suspendreCompte) tente de se connecter.
 * Mappee sur 403 FORBIDDEN -- les identifiants sont corrects, mais l'acces
 * est explicitement bloque.
 */
public class CompteSuspenduException extends RuntimeException {
    public CompteSuspenduException(String message) {
        super(message);
    }
}
