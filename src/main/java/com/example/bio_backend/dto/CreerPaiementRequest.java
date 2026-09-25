package com.example.bio_backend.dto;

/**
 * Corps de la requete POST /api/paiements (initier un paiement pour une commande).
 *
 * Remarque importante : il n'y a volontairement PAS de champ "montant" ici.
 * Comme pour le prix des produits dans une commande (voir CommandeService),
 * on ne fait jamais confiance a un montant envoye par le client -- le
 * montant est toujours relu depuis Commande.montantTotal, cote serveur.
 */
public class CreerPaiementRequest {

    private Long commandeId;
    private String methode;

    public Long getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Long commandeId) {
        this.commandeId = commandeId;
    }

    public String getMethode() {
        return methode;
    }

    public void setMethode(String methode) {
        this.methode = methode;
    }
}
