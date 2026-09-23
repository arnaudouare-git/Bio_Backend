package com.example.bio_backend.exception;

/**
 * Levee quand un Producteur authentifie tente de modifier ou supprimer un
 * produit qui ne lui appartient pas -- c'est-a-dire quand l'id du
 * Producteur proprietaire du produit trouve en base ne correspond pas a
 * l'utilisateur identifie via le token JWT de la requete.
 *
 * Un Administrateur, lui, n'est jamais concerne par cette verification
 * (il modere la plateforme, donc peut agir sur n'importe quel produit --
 * voir ProduitService.verifierProprietaireOuAdmin()).
 */
public class ProduitNonProprietaireException extends RuntimeException {
    public ProduitNonProprietaireException(String message) {
        super(message);
    }
}
