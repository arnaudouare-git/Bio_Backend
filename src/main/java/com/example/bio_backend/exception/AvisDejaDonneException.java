package com.example.bio_backend.exception;

/**
 * Levee quand un acheteur tente de laisser un second avis pour le meme
 * (auteur, commande, producteur). Mappee sur 409 CONFLICT (la requete est
 * valide en soi, mais entre en conflit avec un avis deja existant).
 */
public class AvisDejaDonneException extends RuntimeException {
    public AvisDejaDonneException(String message) {
        super(message);
    }
}
