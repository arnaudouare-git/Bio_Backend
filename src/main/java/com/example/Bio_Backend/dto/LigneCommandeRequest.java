package com.example.bio_backend.dto;

/**
 * Une ligne du "panier" envoye dans CreerCommandeRequest : un produit et une
 * quantite. Le prix n'est jamais fourni par le client -- il est toujours lu
 * depuis Produit.prixUnitaire au moment de la commande, cote serveur, pour
 * eviter qu'un client trafique le prix dans la requete.
 */
public class LigneCommandeRequest {

    private Long produitId;
    private Integer quantite;

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }
}
