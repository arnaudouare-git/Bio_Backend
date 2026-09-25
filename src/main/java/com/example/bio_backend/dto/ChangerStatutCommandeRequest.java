package com.example.bio_backend.dto;

/**
 * Corps de la requete PUT /api/commandes/{id}/statut et
 * PUT /api/commandes/{commandeId}/lignes/{ligneId}/statut. Valeurs
 * acceptees : EN_ATTENTE, CONFIRMEE, EN_PREPARATION, EXPEDIEE, LIVREE,
 * ANNULEE, REFUSEE.
 *
 * "motif" (ajoute le 2026-09-24, module Refus de commande) : obligatoire
 * uniquement quand statut == "REFUSEE" -- la raison pour laquelle le
 * Producteur (ou l'Admin) ne peut pas honorer cette ligne. Ignore pour
 * toute autre valeur de statut.
 */
public class ChangerStatutCommandeRequest {

    private String statut;
    private String motif;

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
