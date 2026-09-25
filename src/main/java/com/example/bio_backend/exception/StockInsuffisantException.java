package com.example.bio_backend.exception;

/**
 * Levee quand une commande demande une quantite superieure au stock
 * disponible pour un produit. Mappee sur 409 CONFLICT (la requete est
 * valide en soi, mais entre en conflit avec l'etat actuel du stock).
 */
public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException(String message) {
        super(message);
    }
}
