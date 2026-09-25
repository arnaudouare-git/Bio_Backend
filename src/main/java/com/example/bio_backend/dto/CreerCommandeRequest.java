package com.example.bio_backend.dto;

import java.util.List;

/**
 * Corps de la requete POST /api/commandes (passer une commande).
 *
 * Depuis le 2026-09-23 : plus de champ "acheteurId" ici. L'acheteur est
 * desormais deduit du token JWT de la requete (voir CommandeController et
 * CommandeService.passerCommande()) -- avant, n'importe qui pouvait passer
 * une commande au nom d'un AUTRE utilisateur simplement en changeant cet
 * id dans le body, meme en etant authentifie avec son propre compte.
 */
public class CreerCommandeRequest {

    private String adresseLivraison;
    private List<LigneCommandeRequest> lignes;

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
