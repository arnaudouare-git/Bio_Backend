package com.example.bio_backend.dto;

/**
 * Corps de la requete PUT /api/commandes/{id}/statut. Valeurs acceptees :
 * EN_ATTENTE, CONFIRMEE, EN_PREPARATION, EXPEDIEE, LIVREE, ANNULEE.
 */
public class ChangerStatutCommandeRequest {

    private String statut;

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
