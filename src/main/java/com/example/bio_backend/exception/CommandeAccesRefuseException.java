package com.example.bio_backend.exception;

/**
 * Levee quand un utilisateur authentifie tente de consulter les commandes
 * (liste ou detail) d'un AUTRE utilisateur -- l'id de l'acheteur associe a
 * la commande ne correspond pas a celui du token JWT de la requete.
 *
 * Un Administrateur, lui, n'est jamais concerne par cette verification
 * (voir CommandeService.verifierProprietaireOuAdmin()).
 */
public class CommandeAccesRefuseException extends RuntimeException {
    public CommandeAccesRefuseException(String message) {
        super(message);
    }
}
