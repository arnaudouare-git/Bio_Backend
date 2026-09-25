package com.example.bio_backend.exception;

/**
 * Levee quand on essaie de :
 * - initier un nouveau paiement pour une commande qui a deja un paiement REUSSI, ou
 * - confirmer un paiement dont le statut n'est plus EN_ATTENTE (deja REUSSI ou ECHOUE).
 * Mappee sur 409 CONFLICT : la requete est valide en soi, mais entre en
 * conflit avec l'etat actuel du paiement/de la commande.
 */
public class PaiementDejaTraiteException extends RuntimeException {
    public PaiementDejaTraiteException(String message) {
        super(message);
    }
}
