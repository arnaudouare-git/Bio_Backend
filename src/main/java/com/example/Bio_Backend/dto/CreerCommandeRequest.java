package com.example.bio_backend.dto;

import java.util.List;

/** Corps de la requete POST /api/commandes (passer une commande). */
public class CreerCommandeRequest {

    private Long acheteurId;
    private String adresseLivraison;
    private List<LigneCommandeRequest> lignes;

    public Long getAcheteurId() {
        return acheteurId;
    }

    public void setAcheteurId(Long acheteurId) {
        this.acheteurId = acheteurId;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public List<LigneCommandeRequest> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeRequest> lignes) {
        this.lignes = lignes;
    }
}
