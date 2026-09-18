package com.example.bio_backend.dto;

import java.math.BigDecimal;

/** Une ligne dans la reponse d'une commande (produit + quantite + sous-total). */
public class LigneCommandeResponse {

    private Long produitId;
    private String produitNom;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;

    public LigneCommandeResponse(Long produitId, String produitNom, Integer quantite,
                                  BigDecimal prixUnitaire, BigDecimal sousTotal) {
        this.produitId = produitId;
        this.produitNom = produitNom;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = sousTotal;
    }

    public Long getProduitId() {
        return produitId;
    }

    public String getProduitNom() {
        return produitNom;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public BigDecimal getSousTotal() {
        return sousTotal;
    }
}
